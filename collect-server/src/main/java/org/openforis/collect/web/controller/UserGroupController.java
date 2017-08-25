package org.openforis.collect.web.controller;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.UserGroupRole;
import org.openforis.collect.model.UserGroup.UserInGroup;
import org.openforis.collect.web.controller.UserController.UserForm;
import org.openforis.collect.web.controller.UserGroupController.UserGroupForm;
import org.openforis.collect.web.validator.UserGroupValidator;
import org.openforis.commons.web.PersistedObjectForm;
import org.springframework.beans.BeanUtils;
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
		private Set<UserInGroupForm> users = new HashSet<UserInGroupForm>();
		
		public UserGroupForm() {
		}
		
		public UserGroupForm(UserGroup userGroup) {
			BeanUtils.copyProperties(userGroup, this, "users");
			this.users = new HashSet<UserInGroupForm>();
			for (UserInGroup user: userGroup.getUsers()) {
				this.users.add(new UserInGroupForm(user));
			}
		}
		
		@Override
		public void copyTo(UserGroup target, String... ignoreProperties) {
			super.copyTo(target, ArrayUtils.addAll(ignoreProperties, "users"));
			Set<UserInGroup> users = new HashSet<UserGroup.UserInGroup>();
			for (UserInGroupForm userInGroupForm : this.users) {
				User user = new User();
				user.setId(userInGroupForm.getId());
				user.setUsername(userInGroupForm.getUsername());
				users.add(new UserInGroup(user, userInGroupForm.getRole()));
			}
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

		public Set<UserInGroupForm> getUsers() {
			return users;
		}

		public void setUsers(Set<UserInGroupForm> users) {
			this.users = users;
		}
	}
	
	public static class UserInGroupForm extends UserForm {
		
		private UserForm user;
		private UserGroupRole role;

		public UserInGroupForm(UserInGroup userInGroup) {
			this.user = new UserForm(userInGroup.getUser());
			this.role = userInGroup.getRole();
		}
		
		public Integer getUserId() {
			return user.getId();
		}

		public boolean isUserEnabled() {
			return user.isEnabled();
		}

		public String getUsername() {
			return user.getUsername();
		}

		public UserGroupRole getRole() {
			return role;
		}

		public void setRole(UserGroupRole role) {
			this.role = role;
		}
	}

}
