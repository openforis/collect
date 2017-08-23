package org.openforis.collect.web.controller;

import java.sql.Timestamp;
import java.util.List;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.web.controller.UserGroupController.UserGroupForm;
import org.openforis.collect.web.validator.UserGroupValidator;
import org.openforis.commons.web.PersistedObjectForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
@RequestMapping("/api/usergroup")
@Scope(WebApplicationContext.SCOPE_SESSION)
public class UserGroupController extends AbstractPersistedObjectEditFormController<UserGroup, UserGroupForm, UserGroupManager> {

	@Autowired
	private UserGroupValidator validator;
	@Autowired
	private SessionManager sessionManager;

	@Override
	@Autowired
	public void setItemManager(UserGroupManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}
	@Override
	protected UserGroup createItemInstance() {
		UserGroup userGroup = new UserGroup();
		userGroup.setCreationDate(new Timestamp(System.currentTimeMillis()));
		userGroup.setCreatedByUser(sessionManager.getSessionState().getUser());
		return userGroup;
	}

	@Override
	protected UserGroupForm createFormInstance(UserGroup item) {
		return new UserGroupForm(item);
	}
	
	@Override
	protected List<UserGroup> loadAllItems() {
		return itemManager.findPublicUserDefinedGroups();
	}
		
	public static class UserGroupForm extends PersistedObjectForm<UserGroup> {
		
		private String    name;
		private String    label;
		private String    description;
		private String    visibilityCode;
		private Boolean   enabled;
		
		public UserGroupForm() {
		}
		
		public UserGroupForm(UserGroup userGroup) {
			super(userGroup);
		}

		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getLabel() {
			return label;
		}
		
		public void setLabel(String label) {
			this.label = label;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
		public String getVisibilityCode() {
			return visibilityCode;
		}
		
		public void setVisibilityCode(String visibilityCode) {
			this.visibilityCode = visibilityCode;
		}
		
		public Boolean getEnabled() {
			return enabled;
		}
		
		public void setEnabled(Boolean enabled) {
			this.enabled = enabled;
		}		
	}

}
