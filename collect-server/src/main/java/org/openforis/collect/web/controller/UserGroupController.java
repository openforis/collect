package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupJoinRequestStatus;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.web.controller.UserGroupController.UserGroupForm;
import org.openforis.collect.web.validator.UserGroupValidator;
import org.openforis.commons.web.PersistedObjectForm;
import org.openforis.commons.web.Response;
import org.openforis.commons.web.SimpleObjectForm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@RequestMapping("/api/usergroup")
@Scope(WebApplicationContext.SCOPE_SESSION)
public class UserGroupController extends AbstractPersistedObjectEditFormController<UserGroup, UserGroupForm, UserGroupManager> {

	@Autowired
	private UserGroupValidator validator;
	@Autowired
	private SessionManager sessionManager;

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
		User loggedUser = sessionManager.getLoggedUser();
		return itemManager.findAllRelatedUserGroups(loggedUser);
	}
	
	@RequestMapping(value="/{userGroupId}/resources/{resourceType}/{resourceId}", method=POST)
	public @ResponseBody Response associateToResource(
			@PathVariable int userGroupId, 
			@PathVariable String resourceType,
			@PathVariable String resourceId) {
		itemManager.associateResource(userGroupId, resourceType, resourceId);
		return new Response();
	}
	
	@RequestMapping(value="/{userGroupId}/resources/{resourceType}/{resourceId}", method=DELETE)
	public @ResponseBody Response disassociateToResource(
			@PathVariable int userGroupId, 
			@PathVariable String resourceType,
			@PathVariable String resourceId) {
		itemManager.disassociateResource(userGroupId, resourceType, resourceId);
		return new Response();
	}
	
	public static class UserGroupForm extends PersistedObjectForm<UserGroup> {
		
		private String    name;
		private String    label;
		private String    description;
		private String    visibilityCode;
		private Integer   parentId;
		private Boolean   enabled;
		private String    qualifierName;
		private String    qualifierValue;
		private List<UserInGroupForm> users = new ArrayList<UserInGroupForm>();
		private List<Integer> childrenGroupIds = new ArrayList<Integer>();
		
		public UserGroupForm() {
		}
		
		public UserGroupForm(UserGroup userGroup) {
			BeanUtils.copyProperties(userGroup, this, "users");
			this.users.clear();
			for (UserInGroup user: userGroup.getUsers()) {
				this.users.add(new UserInGroupForm(user));
			}
			this.childrenGroupIds = new ArrayList<Integer>(userGroup.getChildrenGroupIds());
		}
		
		@Override
		public void copyTo(UserGroup target, String... ignoreProperties) {
			super.copyTo(target, ArrayUtils.addAll(ignoreProperties, "users"));
			Set<UserInGroup> users = new HashSet<UserInGroup>();
			for (UserInGroupForm userInGroupForm : this.users) {
				UserInGroup userInGroup = new UserInGroup();
				userInGroup.setGroupId(target.getId());
				userInGroup.setUserId(userInGroupForm.getUserId());
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
		
		public Integer getParentId() {
			return parentId;
		}
		
		public void setParentId(Integer parentId) {
			this.parentId = parentId;
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
		
		public List<Integer> getChildrenGroupIds() {
			return childrenGroupIds;
		}
		
		public void setChildrenGroupIds(List<Integer> childrenGroupIds) {
			this.childrenGroupIds = childrenGroupIds;
		}
	}
	
	public static class UserInGroupForm extends SimpleObjectForm<UserInGroup> {
		
		private UserGroupRole role;
		private Integer groupId;
		private Integer userId;
		private UserGroupJoinRequestStatus joinStatus;
		private Date joinRequestDate;
		private Date memberSince;

		public UserInGroupForm() {
		}
		
		public UserInGroupForm(UserInGroup userInGroup) {
			this.groupId = userInGroup.getGroupId();
			this.userId = userInGroup.getUserId();
			this.role = userInGroup.getRole();
			this.joinStatus = userInGroup.getJoinStatus();
			this.joinRequestDate = userInGroup.getRequestDate();
			this.memberSince = userInGroup.getMemberSince();
		}
		
		public Integer getGroupId() {
			return groupId;
		}
		
		public void setGroupId(Integer groupId) {
			this.groupId = groupId;
		}
		
		public Integer getUserId() {
			return userId;
		}

		public void setUserId(Integer userId) {
			this.userId = userId;
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