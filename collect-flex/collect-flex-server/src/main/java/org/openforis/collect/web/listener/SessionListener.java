/**
 * 
 */
package org.openforis.collect.web.listener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.web.session.SessionState;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author M. Togna
 * 
 */
public class SessionListener implements HttpSessionListener {

	//private static Log LOG = LogFactory.getLog(SessionListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		String sessionId = session.getId();
		SessionState sessionState = new SessionState(sessionId);
		session.setAttribute(SessionState.SESSION_ATTRIBUTE_NAME, sessionState);
		
		//remove user from security conxtext holder
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		ServletContext servletContext = session.getServletContext();
		Object object = session.getAttribute(SessionState.SESSION_ATTRIBUTE_NAME);
		if (object != null) {
			SessionState sessionState = (SessionState) object;
			CollectRecord record = sessionState.getActiveRecord();
			User user = sessionState.getUser();
			if (record != null && record.getId() != null && user != null) {
				WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
				RecordManager recordManager = (RecordManager) applicationContext.getBean("recordManager");
				recordManager.releaseLock(record.getId());
			}
		}

	}

}
