package org.openforis.collect.designer.viewmodel;

import java.util.Map;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseVM {

	public String getComponentsPath() {
		return Resources.COMPONENTS_BASE_PATH;
	}
	
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
	
	public String getCurrentLanguageCode() {
		SessionStatus sessionStatus = getSessionStatus();
		return sessionStatus.getCurrentLanguageCode();
	}
	
	protected Session getSession() {
		Session session = Executions.getCurrent().getSession();
		return session;
	}
	
	protected Window openPopUp(String url, boolean modal) {
		return openPopUp(url, modal, null);
	}
	
	protected Window openPopUp(String url, boolean modal, Map<String, Object> args) {
		return PopUpUtil.openPopUp(url, modal, args);
	}
	
	protected void closePopUp(Window popUp) {
		PopUpUtil.closePopUp(popUp);
	}
	
	protected void notifyChange(String ... properties) {
		for (String property : properties) {
			BindUtils.postNotifyChange(null, null, this, property);
		}
	}
	
}
