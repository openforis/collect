package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;

public class LocalUserGroupManager implements UserGroupManager {

	@Override
	public String getDefaultPrivateUserGroupName(User user) {
		return user.getUsername() + DEFAULT_PRIVATE_USER_GROUP_NAME_SUFFIX;
	}

	@Override
	public UserGroup findById(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserGroup findByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserGroup> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserGroup> findPublicUserGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserGroup> findByUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserGroup save(UserGroup userGroup) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(long userGroupId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UserGroup findUserGroupByResource(String resourceType, String resourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> findResourcesByUserGroup(long userGroupId, String resourceType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void associateResource(long userGroupId, String resourceType, String resourceId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disassociateResource(long userGroupId, String resourceType, String resourceId) {
		// TODO Auto-generated method stub
		
	}

}
