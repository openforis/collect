package org.openforis.collect.manager;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.granite.context.GraniteContext;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.session.SessionState;
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
	// private static final String ACTIVE_RECORD_SESSION_ATTRIBUTE_NAME = "activeRecord";
	// private static final String USER_SESSION_ATTRIBUTE_NAME = "user";
	private static final String KEEP_ALIVE_SESSION_ATTRIBUTE_NAME = "keepAlive";

	public SessionState getSessionState() {
		SessionState sessionState = (SessionState) getSessionAttribute(SESSION_STATE_SESSION_ATTRIBUTE_NAME);
		if (sessionState == null) {
			sessionState = new SessionState();
			setSessionAttribute(SESSION_STATE_SESSION_ATTRIBUTE_NAME, sessionState);
		}

		// CollectRecord activeRecord = this.getActiveRecord();
		// sessionState.setActiveRecord(activeRecord);

		User user = this.getLoggedInUser();
		sessionState.setUser(user);

		return sessionState;
	}

	public void setActiveRecord(CollectRecord record) {
		// FlexContext.getHttpRequest().getSession().setAttribute(ACTIVE_RECORD_SESSION_ATTRIBUTE_NAME, record);
		SessionState sessionState = getSessionState();
		sessionState.setActiveRecord(record);
	}

	public void clearActiveRecord() {
		// FlexContext.getHttpRequest().getSession().setAttribute(ACTIVE_RECORD_SESSION_ATTRIBUTE_NAME, null);
		SessionState sessionState = getSessionState();
		sessionState.setActiveRecord(null);
	}

	public void keepSessionAlive() {
		setSessionAttribute(KEEP_ALIVE_SESSION_ATTRIBUTE_NAME, new Date());
		//HttpSession session = FlexContext.getHttpRequest().getSession();
		//session.setAttribute(KEEP_ALIVE_SESSION_ATTRIBUTE_NAME, new Date());
		if (LOG.isDebugEnabled()) {
			LOG.debug("Keep alive request received");
		}
	}

	// private Record getActiveRecord() {
	// Record record = (Record) FlexContext.getHttpRequest().getSession().getAttribute(ACTIVE_RECORD_SESSION_ATTRIBUTE_NAME);
	// return record;
	// }

	private User getLoggedInUser() {
		SessionState sessionState = (SessionState) getSessionAttribute(SESSION_STATE_SESSION_ATTRIBUTE_NAME);
		User user = sessionState.getUser();
		if (user == null) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String name = authentication.getName();
			user = new User(name);
			Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
			for (GrantedAuthority grantedAuthority : authorities) {
				user.addAuthority(grantedAuthority.getAuthority());
			}
			// FlexContext.getHttpRequest().getSession().setAttribute(USER_SESSION_ATTRIBUTE_NAME, user);
		}
		return user;
	}
	
	private Object getSessionAttribute(String attributeName) {
		//blazeds
		//FlexContext.getHttpRequest().getSession().getAttribute(attributeName);

		//graniteds
		Object result = GraniteContext.getCurrentInstance().getSessionMap().get(attributeName);
		return result;
	}
	
	private void setSessionAttribute(String attributeName, Object value) {
		//blazeds
		//FlexContext.getHttpRequest().getSession().setAttribute(attributeName, value);
		
		//graniteds
		GraniteContext.getCurrentInstance().getSessionMap().put(attributeName, value);
	}
}
