package org.openforis.collect.manager;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.openforis.collect.config.CollectConfiguration;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.persistence.SurveyStoreException;
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
public class SessionManager {

	private static final Logger LOG = LogManager.getLogger(SessionManager.class);
	private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	@Autowired
	private transient SurveyManager surveyManager;
	@Autowired
	private transient UserManager userManager;
	@Autowired
	private transient RecordManager recordManager;
	
	public void createSessionState(HttpSession session) {
		String sessionId = session.getId();
		boolean developmentMode = CollectConfiguration.isDevelopmentMode();
		SessionState sessionState = new SessionState(sessionId);
		if (developmentMode) {
			sessionState.setUser(userManager.loadAdminUser());
//			sessionState.setUser(userManager.loadByUserName("view"));
//			sessionState.setUser(userManager.loadByUserName("entry"));
		}
		session.setAttribute(SessionState.SESSION_ATTRIBUTE_NAME, sessionState);
	}

	public void sessionDestroyed() {
		CollectRecord activeRecord = getActiveRecord();
		if (activeRecord != null) {
			try {
				releaseRecord();
			} catch (RecordUnlockedException e) {}
		}
	}

	public SessionState getSessionState() {
		SessionState sessionState = (SessionState) getSessionAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
		if (sessionState == null) {
			return null;
		} else {
			if (sessionState.getUser() == null) {
				sessionState.setUser(loadAuthenticatedUser());
			}
			return sessionState;
		}
	}

	public CollectRecord getActiveRecord() {
		SessionState sessionState = getSessionState();
		return sessionState.getActiveRecord();
	}
	
	public User getLoggedUser() {
		SessionState sessionState = getSessionState();
		return sessionState.getUser();
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

	public void setActiveSurvey(CollectSurvey survey) {
		SessionState sessionState = getSessionState();
		sessionState.setActiveSurvey(survey);
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
				surveyManager.save(survey);
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
	
	public void setLocale(String localeStr) {
		Locale locale = toLocale(localeStr);
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
		return result;
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
		sessionState.setActiveRecord(null);
	}
	
	private User loadAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			String name = authentication.getName();
			User user = userManager.loadByUserName(name);
			return user;
		} else {
			return null;
		}
	}
	
	private Locale toLocale(String localeStr) {
		try {
			return LocaleUtils.toLocale(localeStr);
		} catch (Exception e) {
			if (localeStr.length() > 2) {
				//try to use only the first two letters (language code)
				return LocaleUtils.toLocale(localeStr.substring(0, 2));
			} else {
				return DEFAULT_LOCALE;
			}
		}
	}
}
