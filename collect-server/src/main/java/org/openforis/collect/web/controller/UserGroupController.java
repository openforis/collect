package org.openforis.collect.web.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.UserGroupJoinRequestStatus;
import org.openforis.collect.model.UserGroup.UserGroupRole;
import org.openforis.collect.model.UserGroup.UserInGroup;
import org.openforis.collect.web.controller.UserGroupController.UserGroupForm;
import org.openforis.collect.web.validator.UserGroupValidator;
import org.openforis.commons.web.PersistedObjectForm;
import org.openforis.commons.web.SimpleObjectForm;
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
		return itemManager.findUserDefinedGroups();
	}
		
	public static class UserGroupForm extends PersistedObjectForm<UserGroup> {
		
		private String    name;
		private String    label;
		private String    description;
		private String    visibilityCode;
		private Boolean   enabled;
		private String    qualifierName;
		private String    qualifierValue;
		private List<UserInGroupForm> users = new ArrayList<UserInGroupForm>();
		
		public UserGroupForm() {
		}
		
		public UserGroupForm(UserGroup userGroup) {
			BeanUtils.copyProperties(userGroup, this, "users");
			this.users.clear();
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
				user.setId(userInGroupForm.getUserId());
				user.setUsername(userInGroupForm.getUsername());
				UserInGroup userInGroup = new UserInGroup();
				userInGroup.setUser(user);
				userInGroup.setRole(userInGroupForm.getRole());
				userInGroup.setJoinStatus(userInGroupForm.getJoinStatus());
				users.add(userInGroup);
			}
			target.setUsers(users);
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

		public String getQualifierName() {
			return qualifierName;
		}
		
		public void setQualifierName(String qualifierName) {
			this.qualifierName = qualifierName;
		}
		
		public String getQualifierValue() {
			return qualifierValue;
		}
		
		public void setQualifierValue(String qualifierValue) {
			this.qualifierValue = qualifierValue;
		}
		
		public List<UserInGroupForm> getUsers() {
			return users;
		}

		public void setUsers(List<UserInGroupForm> users) {
			this.users = users;
		}
	}
	
	public static class UserInGroupForm extends SimpleObjectForm<UserInGroup> {
		
		private UserGroupRole role;
		private Integer userId;
		private String username;
		private Boolean userEnabled;
		private UserGroupJoinRequestStatus joinStatus;
		private Date joinRequestDate;
		private Date memberSince;

		public UserInGroupForm() {
		}
		
		public UserInGroupForm(UserInGroup userInGroup) {
			User user = userInGroup.getUser();
			this.userId = user.getId();
			this.username = user.getUsername();
			this.userEnabled = user.getEnabled();
			this.role = userInGroup.getRole();
			this.joinStatus = userInGroup.getJoinStatus();
			this.joinRequestDate = userInGroup.getRequestDate();
			this.memberSince = userInGroup.getMemberSince();
		}
		
		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public Boolean getUserEnabled() {
			return userEnabled;
		}

		public void setUserEnabled(Boolean userEnabled) {
			this.userEnabled = userEnabled;
		}

		public UserGroupRole getRole() {
			return role;
		}

		public void setRole(UserGroupRole role) {
			this.role = role;
		}
		
		public UserGroupJoinRequestStatus getJoinStatus() {
			return joinStatus;
		}
		
		public void setJoinStatus(UserGroupJoinRequestStatus joinStatus) {
			this.joinStatus = joinStatus;
		}
		
		public Date getJoinRequestDate() {
			return joinRequestDate;
		}
		
		public Date getMemberSince() {
			return memberSince;
		}
		
	}
}