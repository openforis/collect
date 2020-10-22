/**
 * 
 */
package org.openforis.collect.web.listener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.User;
import org.openforis.collect.web.session.InvalidSessionException;
import org.openforis.collect.web.session.SessionState;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class SessionListener implements HttpSessionListener {

	private static final Logger LOG = LogManager.getLogger(SessionListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		SessionManager sessionManager = getSessionManager(se);
		sessionManager.createSessionState(se.getSession());

		logSessionStatusChange(se, sessionManager, true);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		SessionManager sessionManager = getSessionManager(se);
		try {
			SessionState sessionState = sessionManager.getSessionState();
			User user = sessionState == null ? null : sessionState.getUser();
			if (user != null) {
				sessionManager.sessionDestroyed();
			}
			logSessionStatusChange(se, sessionManager, false);
		} catch (InvalidSessionException e) {
			// ignore it, session was anonymous
		}
	}

	private void logSessionStatusChange(HttpSessionEvent se, SessionManager sessionManager, boolean created) {
		if (LOG.isInfoEnabled()) {
			SessionState sessionState = sessionManager.getSessionState();
			User user = sessionState == null ? null : sessionState.getUser();

			LOG.info("Session " + (created ? "created" : "destroyed") + ": " + se.getSession().getId()
					+ (user == null ? "" : " user: " + user.getUsername()));
		}
	}

	private SessionManager getSessionManager(HttpSessionEvent se) {
		return getBean(se, "sessionManager");
	}

	@SuppressWarnings("unchecked")
	private <T extends Object> T getBean(HttpSessionEvent se, String name) {
		ServletContext sc = se.getSession().getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
		return (T) applicationContext.getBean(name);
	}

}
