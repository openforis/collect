package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.session.SessionStatus;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class BaseVM {

	protected SessionStatus getSessionStatus() {
		Session session = getSession();
		String key = SessionStatus.SESSION_KEY;
		SessionStatus sessionStatus = (SessionStatus) session.getAttribute(key);
		if ( sessionStatus == null ) {
			sessionStatus = new SessionStatus();
			session.setAttribute(key, sessionStatus);
		}
		return sessionStatus;
	}
	
	protected Session getSession() {
		Session session = Executions.getCurrent().getSession();
		return session;
	}
	
	protected void closePopUp(Window popUp) {
		Event event = new Event("onClose", popUp, null);
		Events.postEvent(event);
	}
	
}
