/**
 * 
 */
package org.openforis.collect.web.listener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.web.session.SessionState;
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
		HttpSession session = se.getSession();
		String sessionId = session.getId();
		SessionState sessionState = new SessionState(sessionId);
		session.setAttribute(SessionState.SESSION_ATTRIBUTE_NAME, sessionState);
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Session created: " + sessionId);
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		ServletContext servletContext = session.getServletContext();
		Object object = session.getAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
		User user = null;
		if (object != null) {
			SessionState sessionState = (SessionState) object;
			CollectRecord record = sessionState.getActiveRecord();
			user = sessionState.getUser();
			if (record != null && record.getId() != null && user != null) {
				WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
				RecordManager recordManager = (RecordManager) applicationContext.getBean("recordManager");
				recordManager.releaseLock(record.getId());
			}
		}
		if ( LOG.isInfoEnabled() ) {
			String message = "Session destroyed: " + session.getId();
			if ( user != null ) {
				message += " username: " +user.getName();
			}
			LOG.info(message);
		}
	}
	
}
