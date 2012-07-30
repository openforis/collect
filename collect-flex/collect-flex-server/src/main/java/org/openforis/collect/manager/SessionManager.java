package org.openforis.collect.manager;

import java.util.Collection;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.web.session.InvalidSessionException;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 
 * @author M. Togna
 */
public class SessionManager {

	private static Log LOG = LogFactory.getLog(SessionManager.class);

	@Autowired
	private UserManager userManager;
	
	@Autowired
	protected RecordManager recordManager;

	@Autowired
	private RecordFileManager fileManager;

	
	public SessionState getSessionState() {
		SessionState sessionState = (SessionState) getSessionAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
		if (sessionState == null) {
			throw new InvalidSessionException();
		}
		User user = getLoggedInUser();
		sessionState.setUser(user);

		return sessionState;
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

	public void keepSessionAlive() {
		getSessionState();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Keep alive request received");
		}
	}
	
	public void setLocale(String string) {
		StringTokenizer stringTokenizer = new StringTokenizer(string, "_");
		int tokens = stringTokenizer.countTokens();
		if (tokens < 1 || tokens > 2) {
			throw new IllegalArgumentException("Invalid locale string: " + string);
		}
		String language = stringTokenizer.nextToken();
		String country = "";
		if (stringTokenizer.hasMoreTokens()) {
			country = stringTokenizer.nextToken();
		}
		Locale locale = new Locale(language, country);
		SessionState sessionState = getSessionState();
		sessionState.setLocale(locale);
	}

	public void checkIsActiveRecordLocked() throws RecordUnlockedException {
		SessionState sessionState = getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		User user = sessionState.getUser();
		String lockId = sessionState.getSessionId();
		if ( record == null ) {
			throw new RecordUnlockedException();
		} else if ( record.getId() != null ) {
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
				Integer userId = userManager.getUserId(name);
				user = new User(userId, name);
				Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
				for (GrantedAuthority grantedAuthority : authorities) {
					user.addRole(grantedAuthority.getAuthority());
				}
			}
			return user;
		} else {
			return null;
		}
	}

	private Object getSessionAttribute(String attributeName) {
		GraniteContext graniteContext = GraniteContext.getCurrentInstance();
		if (graniteContext != null) {
			Object result = graniteContext.getSessionMap().get(attributeName);
			return result;
		} else {
			throw new IllegalStateException("Error getting session info");
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
		String sessionId = sessionState.getSessionId();
		fileManager.deleteAllTempFiles(sessionId);
		sessionState.setActiveRecord(null);
	}
	
}
