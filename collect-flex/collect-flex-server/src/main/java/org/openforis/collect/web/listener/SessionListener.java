/**
 * 
 */
package org.openforis.collect.web.listener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.session.SessionState;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author M. Togna
 * 
 */
public class SessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent se) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		ServletContext servletContext = session.getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		if (applicationContext != null) {
			RecordManager recordManager = (RecordManager) applicationContext.getBean("recordManager");
			SessionManager sessionManager = (SessionManager) applicationContext.getBean("sessionManager");
			SessionState sessionState = sessionManager.getSessionState();
			if (sessionState != null) {
				CollectRecord record = sessionState.getActiveRecord();
				recordManager.unlock(record);
			}
		}
	}

}
