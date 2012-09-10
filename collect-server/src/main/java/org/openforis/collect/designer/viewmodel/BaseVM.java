package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.session.SessionStatus;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;

/**
 * 
 * @author S. Ricci
 *
 */
public class BaseVM {

	protected static final String SESSION_STATUS_KEY = "designer_status";
	
	protected SessionStatus getSessionStatus() {
		Session session = getSession();
		SessionStatus sessionStatus = (SessionStatus) session.getAttribute(SESSION_STATUS_KEY);
		if ( sessionStatus == null ) {
			sessionStatus = new SessionStatus();
			session.setAttribute(SESSION_STATUS_KEY, sessionStatus);
		}
		return sessionStatus;
	}
	
	protected Session getSession() {
		Session session = Executions.getCurrent().getSession();
		return session;
	}
}
