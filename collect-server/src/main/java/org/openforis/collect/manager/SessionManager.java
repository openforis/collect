package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.web.session.InvalidSessionException;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 
 * @author M. Togna
 */
public class SessionManager implements EventListener {

	private static Log LOG = LogFactory.getLog(SessionManager.class);

	@Autowired
	private transient SurveyManager surveyManager;
	@Autowired
	private transient UserManager userManager;
	@Autowired
	private transient RecordManager recordManager;
	@Autowired
	private transient SessionRecordFileManager fileManager;
	
	private transient List<RecordEvent> pendingEvents = new CopyOnWriteArrayList<RecordEvent>();
	
	@Override
	public void onEvents(List<? extends RecordEvent> events) {
		//TODO filter events by active record
		pendingEvents.addAll(events);
	}
	
	public SessionState getSessionState() {
		SessionState sessionState = (SessionState) getSessionAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
		if (sessionState == null) {
			throw new InvalidSessionException();
		}
		User user = getLoggedInUser();
		sessionState.setUser(user);

		return sessionState;
	}
	
	public CollectRecord getActiveRecord() {
		SessionState sessionState = getSessionState();
		return sessionState.getActiveRecord();
	}

	public CollectSurvey getActiveDesignerSurvey() {
		SessionStatus designerSessionStatus = getDesignerSessionStatus();
		if ( designerSessionStatus == null ) {
			return null;
		} else {
			return designerSessionStatus.getSurvey();
		}
	}

	public SessionStatus getDesignerSessionStatus() {
		SessionStatus designerSessionStatus = (SessionStatus) getSessionAttribute(SessionStatus.SESSION_KEY);
		return designerSessionStatus;
	}

	public CollectSurvey getActiveSurvey() {
		SessionState sessionState = getSessionState();
		return sessionState.getActiveSurvey();
	}
	
	public void setActiveRecord(CollectRecord record) {
		SessionState sessionState = getSessionState();
		sessionState.setActiveRecord(record);
		sessionState.keepActiveRecordAlive();
	}

	public void clearActiveRecord() {
		SessionState sessionState = getSessionState();
		sessionState.setActiveRecord(null);
	}

	public void saveActiveDesignerSurvey() {
		try {
			SessionState sessionState = getSessionState();
			CollectSurvey survey = getActiveDesignerSurvey();
			boolean activeSurveyWork = sessionState.isActiveSurveyWork();
			if ( activeSurveyWork ) {
				surveyManager.saveSurveyWork(survey);
			} else {
				throw new IllegalArgumentException("Active designer survey should be a 'work' survey");
			}
		} catch ( SurveyStoreException e ) {
			LOG.error("Error updating taxonomy related attributes.", e);
		}
	}
	
	public void keepSessionAlive() {
		getSessionState();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Keep alive request received");
		}
	}
	
	public void setLocale(String string) {
		Locale locale = LocaleUtils.toLocale(string);
		SessionState sessionState = getSessionState();
		sessionState.setLocale(locale);
	}

	public void checkIsActiveRecordLocked() throws RecordUnlockedException {
		SessionState sessionState = getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		if ( record == null ) {
			throw new RecordUnlockedException();
		} else if ( record.getId() != null ) {
			User user = sessionState.getUser();
			String lockId = sessionState.getSessionId();
			try {
				recordManager.checkIsLocked(record.getId(), user, lockId);
				sessionState.keepActiveRecordAlive();
			} catch (RecordUnlockedException e) {
				clearActiveRecord();
				throw e;
			}
		}
	}
	
	private User getLoggedInUser() {
		SessionState sessionState = (SessionState) getSessionAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
		if (sessionState != null) {
			User user = sessionState.getUser();
			if (user == null) {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				String name = authentication.getName();
				user = userManager.loadByUserName(name);
			}
			return user;
		} else {
			return null;
		}
	}

	private Object getSessionAttribute(String attributeName) {
		Object result = null;
		
		//try to get session attribute from GraniteDS context
		GraniteContext graniteContext = GraniteContext.getCurrentInstance();
		if (graniteContext != null) {
			result = graniteContext.getSessionMap().get(attributeName);
		} else {
			//try to get session attribute from current request context holder session
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if ( requestAttributes != null ) {
				HttpSession session = requestAttributes.getRequest().getSession();
				result = session.getAttribute(attributeName);
			}
		}
		if ( result == null ) {
			throw new IllegalStateException("Error getting session attribute: " + attributeName);
		} else {
			return result;
		}
	}
	
	public void invalidateSession() {
		try {
			releaseRecord();
		} catch (RecordUnlockedException e) {
			//do nothing
		}
		GraniteContext graniteContext = GraniteContext.getCurrentInstance();
		if ( graniteContext != null && graniteContext instanceof HttpGraniteContext ) {
			HttpGraniteContext httpGraniteContext = (HttpGraniteContext) graniteContext;
			HttpServletRequest request = httpGraniteContext.getRequest();
			HttpSession session = request.getSession();
			session.invalidate();
		}
	}

	public void releaseRecord() throws RecordUnlockedException {
		checkIsActiveRecordLocked();
		SessionState sessionState = getSessionState();
		CollectRecord activeRecord = sessionState.getActiveRecord();
		if ( activeRecord != null && activeRecord.getId() != null ) {
			recordManager.releaseLock(activeRecord.getId());
		}
		fileManager.deleteAllTempFiles();
		sessionState.setActiveRecord(null);
		
		pendingEvents.clear();
	}

	public List<RecordEvent> flushPendingEvents() {
		if (pendingEvents.isEmpty()) {
			return Collections.emptyList();
		}
		List<RecordEvent> events = new ArrayList<RecordEvent>(pendingEvents);
		pendingEvents.clear();
		return events;
	}

}
