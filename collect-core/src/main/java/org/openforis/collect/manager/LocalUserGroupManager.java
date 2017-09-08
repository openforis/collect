package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.UserGroupJoinRequestStatus;
import org.openforis.collect.model.UserGroup.UserGroupRole;
import org.openforis.collect.model.UserGroup.UserInGroup;
import org.openforis.collect.model.UserGroup.Visibility;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.persistence.UserGroupDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class LocalUserGroupManager extends AbstractPersistedObjectManager<UserGroup, Integer, UserGroupDao> implements UserGroupManager {

	private static final String DEFAULT_PUBLIC_USER_GROUP_NAME = "default_public_group";
	private static final String DEFAULT_PRIVATE_USER_GROUP_SUFFIX = "_default_private_group";
	private static final String DEFAULT_PRIVATE_USER_GROUP_LABEL_SUFFIX = " Default Private Group";
	
	private static UserGroup DEFAULT_PUBLIC_USER_GROUP = null;
	
	@Override
	@Autowired
	public void setDao(UserGroupDao dao) {
		super.setDao(dao);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public UserGroup createDefaultPrivateUserGroup(User user) {
		UserGroup userGroup = new UserGroup();
		userGroup.setName(getDefaultPrivateUserGroupName(user));
		userGroup.setLabel(user.getUsername() + DEFAULT_PRIVATE_USER_GROUP_LABEL_SUFFIX);
		userGroup.setVisibility(Visibility.PRIVATE);
		dao.insert(userGroup);
		UserInGroup userInGroup = new UserInGroup();
		userInGroup.setUser(user);
		userInGroup.setRole(UserGroupRole.OWNER);
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
	
	@Transactional(propagation=Propagation.REQUIRED)
	public UserGroup save(UserGroup userGroup) {
		dao.save(userGroup);
		List<UserInGroup> oldUsersInGroup = dao.findUsersByGroup(userGroup);
		Set<UserInGroup> parameterUsersInGroup = userGroup.getUsers();
		Set<UserInGroup> removedUsersInGroup = new HashSet<UserInGroup>();
		Set<UserInGroup> updatedUsersInGroup = new HashSet<UserInGroup>();
		Set<UserInGroup> newUsersInGroup = new HashSet<UserInGroup>();

		for (UserInGroup oldUserInGroup : oldUsersInGroup) {
			if (parameterUsersInGroup.contains(oldUserInGroup)) {
				final User user = oldUserInGroup.getUser();
				UserInGroup modifiedUserInGroup = (UserInGroup) CollectionUtils.find(parameterUsersInGroup, new Predicate() {
					public boolean evaluate(Object parameterUserInGroup) {
						return user.equals(((UserInGroup) parameterUserInGroup).getUser());
					}
				});
				oldUserInGroup.setJoinStatus(modifiedUserInGroup.getJoinStatus());
				updatedUsersInGroup.add(oldUserInGroup);
			} else {
				removedUsersInGroup.add(oldUserInGroup);
			}
		}

		for (UserInGroup newUserInGroup : parameterUsersInGroup) {
			if (! oldUsersInGroup.contains(newUserInGroup)) {
				newUserInGroup.setRequestDate(new Date());
				if (newUserInGroup.getJoinStatus() == UserGroupJoinRequestStatus.ACCEPTED) {
					newUserInGroup.setMemberSince(new Date());
				}
				newUsersInGroup.add(newUserInGroup);
			}
		}

		for (UserInGroup userInGroup : removedUsersInGroup) {
			dao.deleteRelation(userInGroup.getUser(), userGroup);
		}
		for (UserInGroup userInGroup : updatedUsersInGroup) {
			dao.updateRelation(userGroup, userInGroup);
		}
		for (UserInGroup userInGroup : newUsersInGroup) {
			dao.insertRelation(userGroup, userInGroup);
		}
		return userGroup;
	}
	
	@Override
	public UserGroup findByName(String userGroupName) {
		return dao.loadByName(userGroupName);
	}

	@Override
	public List<UserInGroup> findUsersByGroup(UserGroup userGroup) {
		return dao.findUsersByGroup(userGroup);
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

	public List<UserGroup> findUserDefinedGroups() {
		return fillLazyLoadedFields(dao.findUserDefinedGroups());
	}

	public List<UserGroup> findPublicUserDefinedGroups() {
		return fillLazyLoadedFields(dao.findPublicUserDefinedGroups());
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void requestJoin(User user, UserGroup userGroup, UserGroupRole role) {
		UserInGroup userInGroup = new UserInGroup();
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
			List<UserInGroup> usersInGroup = dao.findUsersByGroup(group);
			group.setUsers(new HashSet<UserInGroup>(usersInGroup));
		}
		return groups;
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
