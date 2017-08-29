package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.User;

public interface UserGroupManager {
	
	static String DEFAULT_PUBLIC_USER_GROUP_NAME = "default_public_group";
	static String DEFAULT_PRIVATE_USER_GROUP_NAME_SUFFIX = "_private_group";

	String getDefaultPrivateUserGroupName(User user);
	
	UserGroup findById(long id);
	
	UserGroup findByName(String name);
	
	List<UserGroup> findAll();

	List<UserGroup> findPublicUserGroups();

	List<UserGroup> findByUser(User user);
	
	UserGroup save(UserGroup userGroup);

	void delete(long userGroupId);

	UserGroup findUserGroupByResource(String resourceType, String resourceId);
	
	List<String> findResourcesByUserGroup(long userGroupId, String resourceType);
	
	void associateResource(long userGroupId, String resourceType, String resourceId);
	
	void disassociateResource(long userGroupId, String resourceType, String resourceId);

}