/**
 * 
 */
package org.openforis.collect.web.listener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.User;
import org.openforis.collect.web.session.InvalidSessionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class SessionListener implements HttpSessionListener {

	private static Log LOG = LogFactory.getLog(SessionListener.class);
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		SessionManager sessionManager = getSessionManager(se);
		sessionManager.createSessionState(se.getSession());
		
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Session created: " + se.getSession().getId());
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		SessionManager sessionManager = getSessionManager(se);
		try {
			User user = sessionManager.getSessionState().getUser();
			if (user != null) {
				sessionManager.sessionDestroyed();
			}
			if ( LOG.isInfoEnabled() ) {
				String message = "Session destroyed: " + se.getSession().getId();
				if ( user != null ) {
					message += " username: " +user.getUsername();
				}
				LOG.info(message);
			}
		} catch(InvalidSessionException e) {
			//ignore it, session was anonymous
		}
//		HttpSession session = se.getSession();
//		Object sessionStateObj = session.getAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
//		User user = null;
//		if (sessionStateObj != null) {
//			SessionState sessionState = (SessionState) sessionStateObj;
//			CollectRecord record = sessionState.getActiveRecord();
//			user = sessionState.getUser();
//			if (record != null && record.getId() != null && user != null) {
//				RecordManager recordManager = getBean(se, RecordManager.class);
//				recordManager.releaseLock(record.getId());
//			}
//			
//		}
	}

	private SessionManager getSessionManager(HttpSessionEvent se) {
		return getBean(se, "sessionManager");
	}

	@SuppressWarnings("unchecked")
	private <T extends Object> T getBean(HttpSessionEvent se,
			String name) {
		ServletContext sc = se.getSession().getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);
		return (T) applicationContext.getBean(name);
	}
	
}
