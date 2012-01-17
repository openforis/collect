package org.openforis.collect.manager;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.granite.context.GraniteContext;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.session.SessionState;
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

	private static final String SESSION_STATE_SESSION_ATTRIBUTE_NAME = "sessionState";
	private static final String KEEP_ALIVE_SESSION_ATTRIBUTE_NAME = "keepAlive";

	@Autowired
	private UserManager userManager;

	public SessionState getSessionState() {
		SessionState sessionState = (SessionState) getSessionAttribute(SESSION_STATE_SESSION_ATTRIBUTE_NAME);
		if (sessionState == null) {
			sessionState = new SessionState();
			setSessionAttribute(SESSION_STATE_SESSION_ATTRIBUTE_NAME, sessionState);
		}

		User user = this.getLoggedInUser();
		sessionState.setUser(user);

		return sessionState;
	}

	public void setActiveRecord(CollectRecord record) {
		SessionState sessionState = getSessionState();
		sessionState.setActiveRecord(record);
	}

	public void clearActiveRecord() {
		SessionState sessionState = getSessionState();
		sessionState.setActiveRecord(null);
	}

	public void keepSessionAlive() {
		setSessionAttribute(KEEP_ALIVE_SESSION_ATTRIBUTE_NAME, new Date());
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

	private User getLoggedInUser() {
		SessionState sessionState = (SessionState) getSessionAttribute(SESSION_STATE_SESSION_ATTRIBUTE_NAME);
		if (sessionState != null) {
			User user = sessionState.getUser();
			if (user == null) {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				String name = authentication.getName();
				Integer userId = userManager.getUserId(name);
				user = new User(userId, name);
				Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
				for (GrantedAuthority grantedAuthority : authorities) {
					user.addAuthority(grantedAuthority.getAuthority());
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
			return null;
		}
	}

	private void setSessionAttribute(String attributeName, Object value) {
		GraniteContext graniteContext = GraniteContext.getCurrentInstance();
		if (graniteContext != null) {
			graniteContext.getSessionMap().put(attributeName, value);
		}
	}
}
