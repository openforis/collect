package org.openforis.collect.designer.viewmodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.designer.model.LabelledItem;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreator;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public abstract class BaseVM {
	
	private static final String PUBLIC_USER_GROUP_LABEL_KEY = "survey.template.usergroup.public";
	private static final String PRIVATE_USER_GROUP_LABEL_KEY = "survey.template.usergroup.private";

	private static final SimpleDateFormat PRETTY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	protected static final ServiceLoader<CollectEarthProjectFileCreator> COLLECT_EARTH_PROJECT_FILE_CREATOR_LOADER = 
			ServiceLoader.load(CollectEarthProjectFileCreator.class);

	@WireVariable
	protected UserManager userManager;
	@WireVariable
	protected CollectJobManager jobManager;
	@WireVariable
	protected UserGroupManager userGroupManager;

	private BindingListModelListModel<LabelledItem> userGroupsModel;

	void init() {
		initUserGroupsModel();
	}
	
	protected void initUserGroupsModel() {
		List<LabelledItem> items = new ArrayList<LabelledItem>();
		User loggedUser = getLoggedUser();
		List<UserGroup> userGroups = userGroupManager.findAllRelatedUserGroups(loggedUser);
		
		for (UserGroup userGroup : userGroups) {
			String label = 
				userGroup.getName().equals(UserGroupManager.DEFAULT_PUBLIC_USER_GROUP_NAME) ?
					Labels.getLabel(PUBLIC_USER_GROUP_LABEL_KEY)
					: userGroup.getName().equals(userGroupManager.getDefaultPrivateUserGroupName(loggedUser)) ? 
						Labels.getLabel(PRIVATE_USER_GROUP_LABEL_KEY)
						: userGroup.getLabel();
			items.add(new LabelledItem(userGroup.getName(), label));
		}
		userGroupsModel = new BindingListModelListModel<LabelledItem>(new ListModelList<LabelledItem>(items));
		userGroupsModel.setMultiple(false);
	}
	
	protected LabelledItem getDefaultPublicUserGroupItem() {
		return userGroupsModel.getElementAt(0);
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
		return authentication.getName();
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
	
	protected <T> T getFormFieldValue(Binder binder, String field) {
		return getFormFieldValue(ComponentUtil.getForm(binder), field);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getFormFieldValue(Form form, String field) {
		return (T) form.getField(field);
	}
	
	protected void setFormFieldValue(Binder binder, String field,
			Object value) {
		setFormFieldValue(ComponentUtil.getForm(binder), field, value);
	}
	
	protected void setFormFieldValue(Form form, String field, Object value) {
		form.setField(field, value);
		BindUtils.postNotifyChange(null, null, form, field);
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
	
	public BindingListModelListModel<LabelledItem> getUserGroupsModel() {
		return userGroupsModel;
	}
}
