package org.openforis.collect.designer.viewmodel;

import java.util.Map;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Form;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseVM {
	
	@WireVariable
	private UserManager userManager;

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
	
	protected User getLoggedUser() {
//		Session session = getSession();
//		HttpSession httpSession = (HttpSession) session.getNativeSession();
		String userName = "admin";
		User user = userManager.loadByUserName(userName);
		return user;
	}
	
	protected static Window openPopUp(String url, boolean modal) {
		return openPopUp(url, modal, null);
	}
	
	protected static Window openPopUp(String url, boolean modal, Map<String, Object> args) {
		return PopUpUtil.openPopUp(url, modal, args);
	}
	
	protected static void closePopUp(Window popUp) {
		PopUpUtil.closePopUp(popUp);
	}
	
	protected void notifyChange(String ... properties) {
		for (String property : properties) {
			BindUtils.postNotifyChange(null, null, this, property);
		}
	}
	
	protected String getInitParameter(String name) {
		WebApp webApp = Sessions.getCurrent().getWebApp();
		return webApp.getInitParameter(name);
	}
	
	protected void setValueOnFormField(Form form, String field,
			Object value) {
		form.setField(field, value);
		BindUtils.postNotifyChange(null, null, form, field);
	}
	
}
