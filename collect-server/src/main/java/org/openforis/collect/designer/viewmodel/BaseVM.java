package org.openforis.collect.designer.viewmodel;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreator;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Form;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public abstract class BaseVM {
	
	private static final SimpleDateFormat PRETTY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:ss");
	
	protected static final ServiceLoader<CollectEarthProjectFileCreator> COLLECT_EARTH_PROJECT_FILE_CREATOR_LOADER = 
			ServiceLoader.load(CollectEarthProjectFileCreator.class);

	@WireVariable
	protected UserManager userManager;
	@WireVariable
	protected CollectJobManager jobManager;

	void init() {
	}
	
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
	
	public User getLoggedUser() {
		String loggedUsername = getLoggedUsername();
		User user = userManager.loadByUserName(loggedUsername);
		return user;
	}
	
	public String getLoggedUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedUsername = authentication.getName();
		return loggedUsername;
	}
	
	protected static Window openPopUp(String url, boolean modal) {
		return openPopUp(url, modal, null);
	}
	
	protected static Window openPopUp(String url, boolean modal, Map<?, ?> args) {
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
	
	protected String adjustInternalName(String name) {
		String result = StringUtils.trimToEmpty(name);
		result = result.toLowerCase(Locale.ENGLISH);
		return result;
	}
	
	public String joinValues(String[] values, String separator) {
		return joinList(Arrays.asList(values), separator);
	}
	
	public String joinList(List<String> values, String separator) {
		return StringUtils.join(values, separator);
	}
	
	public String prettyDateFormat(Date date) {
		return PRETTY_DATE_FORMAT.format(date);
	}
}

