package org.openforis.collect.manager;

import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.openforis.collect.model.SessionState;
import org.openforis.collect.model.User;
import org.openforis.collect.util.LogUtils;
import org.openforis.idm.model.Record;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import flex.messaging.FlexContext;

/**
 * 
 * @author M. Togna
 */
public class SessionManager {

	private static Log LOG = LogUtils.getLog(SessionManager.class);

	private static final String SESSION_STATE_SESSION_ATTRIBUTE_NAME = "sessionState";
	private static final String ACTIVE_RECORD_SESSION_ATTRIBUTE_NAME = "sessionState";
	private static final String USER_SESSION_ATTRIBUTE_NAME = "sessionState";
	private static final String KEEP_ALIVE_SESSION_ATTRIBUTE_NAME = "keepAlive";

	public SessionState getSessionState() {
		SessionState sessionState = (SessionState) FlexContext.getHttpRequest().getSession().getAttribute(SESSION_STATE_SESSION_ATTRIBUTE_NAME);
		if (sessionState == null) {
			sessionState = new SessionState();
			FlexContext.getHttpRequest().getSession().setAttribute(SESSION_STATE_SESSION_ATTRIBUTE_NAME, sessionState);
		}

		Record activeRecord = this.getActiveRecord();
		sessionState.setActiveRecord(activeRecord);

		User user = this.getLoggedInUser();
		sessionState.setUser(user);

		return sessionState;
	}

	public void setActiveRecord(Record record) {
		FlexContext.getHttpRequest().getSession().setAttribute(ACTIVE_RECORD_SESSION_ATTRIBUTE_NAME, record);
	}

	public void clearActiveRecord() {
		FlexContext.getHttpRequest().getSession().setAttribute(ACTIVE_RECORD_SESSION_ATTRIBUTE_NAME, null);
	}

	public void keepSessionAlive() {
		HttpSession session = FlexContext.getHttpRequest().getSession();
		session.setAttribute(KEEP_ALIVE_SESSION_ATTRIBUTE_NAME, new Date());
		if (LOG.isDebugEnabled()) {
			LOG.debug("Keep alive request received");
		}
	}

	private Record getActiveRecord() {
		Record record = (Record) FlexContext.getHttpRequest().getSession().getAttribute(ACTIVE_RECORD_SESSION_ATTRIBUTE_NAME);
		return record;
	}

	private User getLoggedInUser() {
		User user = (User) FlexContext.getHttpRequest().getSession().getAttribute(USER_SESSION_ATTRIBUTE_NAME);
		if (user == null) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String name = authentication.getName();
			user = new User(name);
			Collection<GrantedAuthority> authorities = authentication.getAuthorities();
			for (GrantedAuthority grantedAuthority : authorities) {
				user.addAuthority(grantedAuthority.getAuthority());
			}

			FlexContext.getHttpRequest().getSession().setAttribute(USER_SESSION_ATTRIBUTE_NAME, user);
		}
		return user;
	}

}
