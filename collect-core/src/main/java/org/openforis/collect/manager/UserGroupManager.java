package org.openforis.collect.manager;

import java.util.List;
import java.util.Map;

import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupRole;

public interface UserGroupManager extends ItemManager<UserGroup, Integer> {
	
	static String DEFAULT_PUBLIC_USER_GROUP_NAME = "default_public_group";
	static String DEFAULT_PRIVATE_USER_GROUP_NAME_SUFFIX = "_private_group";

	UserGroup getDefaultPublicUserGroup();

	String getDefaultPrivateUserGroupName(User user);
	
	UserGroup findByName(String name);
	
	List<UserGroup> loadAll();
	
	List<UserGroup> findAllUserDefinedGroups();
	
	List<UserGroup> findAllRelatedUserGroups(User user);
	
	List<UserGroup> findPublicUserGroups();

	List<UserGroup> findDescendantGroups(UserGroup group);

	List<UserGroup> findByUser(User user);
	
	List<UserInGroup> findUsersInGroup(UserGroup userGroup);
	
	UserInGroup findUserInGroup(UserGroup userGroup, User user);
	
	UserInGroup findUserInGroupOrDescendants(UserGroup userGroup, User user);

	UserGroup findUserGroupByResource(String resourceType, String resourceId);
	
	List<String> findResourcesByUserGroup(int userGroupId, String resourceType);
	
	Map<String, String> getQualifiers(UserGroup group, User user);
	
	void associateResource(int userGroupId, String resourceType, String resourceId);
	
	void disassociateResource(int userGroupId, String resourceType, String resourceId);
	
	void deleteRelation(int userGroupId, int userId);

	void deleteAllUserRelations(int userId);

	UserGroup createDefaultPrivateUserGroup(User user, User createdByUser);

	void joinToDefaultPublicGroup(User user, UserGroupRole role);
}
