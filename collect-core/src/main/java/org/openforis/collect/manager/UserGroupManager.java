package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.UserInGroup;
import org.openforis.collect.model.User;

public interface UserGroupManager {
	
	static String DEFAULT_PUBLIC_USER_GROUP_NAME = "default_public_group";
	static String DEFAULT_PRIVATE_USER_GROUP_NAME_SUFFIX = "_private_group";

	UserGroup getDefaultPublicUserGroup();

	String getDefaultPrivateUserGroupName(User user);
	
	UserGroup findById(int id);
	
	UserGroup findByName(String name);
	
	List<UserGroup> findAll();

	List<UserGroup> findPublicUserGroups();

	List<UserGroup> findByUser(User user);
	
	List<UserInGroup> findUsersByGroup(UserGroup userGroup);

	UserGroup save(UserGroup userGroup);

	void delete(int userGroupId);

	UserGroup findUserGroupByResource(String resourceType, String resourceId);
	
	List<String> findResourcesByUserGroup(int userGroupId, String resourceType);
	
	void associateResource(int userGroupId, String resourceType, String resourceId);
	
	void disassociateResource(int userGroupId, String resourceType, String resourceId);

}
