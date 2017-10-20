package org.openforis.collect.manager;

import static org.openforis.collect.model.UserGroup.UserGroupRole.ADMINISTRATOR;
import static org.openforis.collect.model.UserGroup.UserGroupRole.OWNER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.UserGroupJoinRequestStatus;
import org.openforis.collect.model.UserGroup.UserGroupRole;
import org.openforis.collect.model.UserGroup.UserInGroup;
import org.openforis.collect.model.UserGroup.Visibility;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.persistence.UserGroupDao;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class LocalUserGroupManager extends AbstractPersistedObjectManager<UserGroup, Integer, UserGroupDao> implements UserGroupManager {

	private static final List<UserGroupRole> ADMINISTRATOR_ROLES = Arrays.asList(ADMINISTRATOR, OWNER);
	private static final String DEFAULT_PUBLIC_USER_GROUP_NAME = "default_public_group";
	private static final String DEFAULT_PRIVATE_USER_GROUP_SUFFIX = "_default_private_group";
	private static final String DEFAULT_PRIVATE_USER_GROUP_LABEL_SUFFIX = " Default Private Group";
	
	private static UserGroup DEFAULT_PUBLIC_USER_GROUP = null;
	
	@Transactional(propagation=Propagation.REQUIRED)
	public UserGroup createDefaultPrivateUserGroup(User user) {
		UserGroup userGroup = new UserGroup();
		userGroup.setName(getDefaultPrivateUserGroupName(user));
		userGroup.setLabel(user.getUsername() + DEFAULT_PRIVATE_USER_GROUP_LABEL_SUFFIX);
		userGroup.setVisibility(Visibility.PRIVATE);
		dao.insert(userGroup);
		UserInGroup userInGroup = new UserInGroup();
		userInGroup.setGroup(userGroup);
		userInGroup.setUser(user);
		userInGroup.setRole(OWNER);
		userInGroup.setJoinStatus(UserGroupJoinRequestStatus.ACCEPTED);
		userInGroup.setRequestDate(new Date());
		userInGroup.setMemberSince(new Date());
		dao.insertRelation(userGroup, userInGroup);
		return userGroup;
	}

	public String getDefaultPrivateUserGroupName(User user) {
		return user.getUsername() + DEFAULT_PRIVATE_USER_GROUP_SUFFIX;
	}
	
	public String getDefaultPublicUserGroupName() {
		return DEFAULT_PUBLIC_USER_GROUP_NAME;
	}

	public UserGroup getDefaultPublicUserGroup() {
		if (DEFAULT_PUBLIC_USER_GROUP == null) {
			DEFAULT_PUBLIC_USER_GROUP = dao.loadByName(getDefaultPublicUserGroupName());
		}
		return DEFAULT_PUBLIC_USER_GROUP;
	}
	
	@Override
	public List<UserGroup> findAllUserDefinedGroups() {
		return fillLazyLoadedFields(dao.findGroups(false, null));
	}
	
	@Override
	public List<UserGroup> findManageableUserGroups(User user) {
		List<UserGroup> result = new ArrayList<UserGroup>();
		List<UserGroup> userDefinedGroups = findAllUserDefinedGroups();
		for (UserGroup userGroup : userDefinedGroups) {
			UserInGroup userInGroup = findUserInGroup(userGroup, user);
			if (userInGroup != null && ADMINISTRATOR_ROLES.contains(userInGroup.getRole())) {
				result.add(userInGroup.getGroup());
			}
		}
		return result;
	}
	
	@Override
	public UserGroup findByName(String userGroupName) {
		return fillLazyLoadedFields(dao.loadByName(userGroupName));
	}

	@Override
	public List<UserInGroup> findUsersInGroup(UserGroup userGroup) {
		return dao.findUsersByGroup(userGroup);
	}
	
	@Override
	public UserInGroup findUserInGroup(UserGroup userGroup, User user) {
		return dao.findUserInGroup(userGroup, user);
	}
	
	@Override
	public UserInGroup findUserInGroupOrDescendants(UserGroup userGroup, User user) {
		UserInGroup userInGroup = findUserInGroup(userGroup, user);
		if (userInGroup == null) {
			List<UserGroup> descendantGroups = dao.findDescendantGroups(userGroup);
			for (UserGroup descendantGroup : descendantGroups) {
				userInGroup = findUserInGroup(descendantGroup, user);
				if (userInGroup != null) {
					return userInGroup;
				}
			}
			return null;
		} else {
			return userInGroup;
		}
	}
	
	@Override
	public List<UserGroup> findByUser(User user) {
		List<UserGroup> result;
		List<UserRole> userRoles = user.getRoles();
		if (userRoles.contains(UserRole.ADMIN)) {
			result = dao.loadAll();
		} else {
			result = dao.findByUser(user);
		}
		return fillLazyLoadedFields(result);
	}
	
	@Override
	public List<UserGroup> findPublicUserGroups() {
		return fillLazyLoadedFields(dao.findPublicGroups());
	}

	public List<UserGroup> findPublicUserDefinedGroups() {
		return fillLazyLoadedFields(dao.findPublicUserDefinedGroups());
	}
	
	@Override
	public List<UserGroup> findDescendantGroups(UserGroup group) {
		return fillLazyLoadedFields(dao.findDescendantGroups(group));
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public UserGroup save(UserGroup userGroup) {
		dao.save(userGroup);
		List<UserInGroup> oldUsersInGroup = dao.findUsersByGroup(userGroup);
		Map<Integer, UserInGroup> parameterUsersInGroupByUserId = new HashMap<Integer, UserInGroup>();
		for (UserInGroup userInGroup : userGroup.getUsers()) {
			parameterUsersInGroupByUserId.put(userInGroup.getUserId(), userInGroup);
		}
		Map<Integer, UserInGroup> removedUsersInGroupByUserId = new HashMap<Integer, UserInGroup>();
		Map<Integer, UserInGroup> updatedUsersInGroupByUserId = new HashMap<Integer, UserInGroup>();
		Map<Integer, UserInGroup> newUsersInGroupByUserId = new HashMap<Integer, UserInGroup>();

		for (UserInGroup oldUserInGroup : oldUsersInGroup) {
			UserInGroup updatedUserInGroup = parameterUsersInGroupByUserId.get(oldUserInGroup.getUserId());
			if (updatedUserInGroup != null) {
				oldUserInGroup.setJoinStatus(updatedUserInGroup.getJoinStatus());
				updatedUsersInGroupByUserId.put(oldUserInGroup.getUserId(), oldUserInGroup);
			} else {
				removedUsersInGroupByUserId.put(oldUserInGroup.getUserId(), oldUserInGroup);
			}
		}

		for (UserInGroup newUserInGroup : parameterUsersInGroupByUserId.values()) {
			if (! oldUsersInGroup.contains(newUserInGroup)) {
				newUserInGroup.setRequestDate(new Date());
				if (newUserInGroup.getJoinStatus() == UserGroupJoinRequestStatus.ACCEPTED) {
					newUserInGroup.setMemberSince(new Date());
				}
				newUsersInGroupByUserId.put(newUserInGroup.getUserId(), newUserInGroup);
			}
		}

		for (UserInGroup userInGroup : removedUsersInGroupByUserId.values()) {
			dao.deleteRelation(userInGroup.getUser(), userGroup);
		}
		for (UserInGroup userInGroup : updatedUsersInGroupByUserId.values()) {
			dao.updateRelation(userInGroup);
		}
		for (UserInGroup userInGroup : newUsersInGroupByUserId.values()) {
			dao.insertRelation(userGroup, userInGroup);
		}
		return userGroup;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void requestJoin(User user, UserGroup userGroup, UserGroupRole role) {
		UserInGroup userInGroup = new UserInGroup();
		userInGroup.setGroup(userGroup);
		userInGroup.setUser(user);
		userInGroup.setRole(role);
		userInGroup.setRequestDate(new Date());
		userInGroup.setJoinStatus(UserGroupJoinRequestStatus.PENDING);
		dao.insertRelation(userGroup, userInGroup);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void acceptJoinRequest(User user, UserGroup userGroup) {
		dao.acceptRelation(user, userGroup);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void delete(int id) {
		dao.deleteById(id);
	}
	
	private List<UserGroup> fillLazyLoadedFields(List<UserGroup> groups) {
		for (UserGroup group : groups) {
			fillLazyLoadedFields(group);
		}
		return groups;
	}

	private UserGroup fillLazyLoadedFields(UserGroup group) {
		List<UserInGroup> usersInGroup = dao.findUsersByGroup(group);
		group.setUsers(new HashSet<UserInGroup>(usersInGroup));
		return group;
	}

	public static class UserGroupTree {
		
		private List<UserGroupTreeNode> roots = new ArrayList<UserGroupTreeNode>();
		
		public List<UserGroupTreeNode> getRoots() {
			return roots;
		}
		
		public static class UserGroupTreeNode {
			private UserGroup userGroup;
			private List<UserGroupTreeNode> children = new ArrayList<UserGroupTreeNode>();
			
			public UserGroupTreeNode(UserGroup userGroup) {
				super();
				this.userGroup = userGroup;
			}
			
			public UserGroup getUserGroup() {
				return userGroup;
			}
			
			public List<UserGroupTreeNode> getChildren() {
				return children;
			}
			
		}
		
	}

	@Override
	public UserGroup findUserGroupByResource(String resourceType, String resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> findResourcesByUserGroup(int userGroupId, String resourceType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void associateResource(int userGroupId, String resourceType, String resourceId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disassociateResource(int userGroupId, String resourceType, String resourceId) {
		// TODO Auto-generated method stub
		
	}

}
