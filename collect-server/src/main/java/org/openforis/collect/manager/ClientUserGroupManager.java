package org.openforis.collect.manager;

import static org.openforis.collect.config.CollectConfiguration.getUsersRestfulApiUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.openforis.collect.client.AbstractClient;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupJoinRequestStatus;
import org.openforis.collect.model.UserInGroup.UserGroupRole;

public class ClientUserGroupManager extends AbstractClient implements UserGroupManager {

	/**
	 * Cached default public user group
	 */
	private UserGroup defaultPublicUserGroup;

	@Override
	public UserGroup getDefaultPublicUserGroup() {
		if (defaultPublicUserGroup == null) {
			defaultPublicUserGroup = findByName(DEFAULT_PUBLIC_USER_GROUP_NAME); 
		}
		return defaultPublicUserGroup;
	}

	@Override
	public String getDefaultPrivateUserGroupName(User user) {
		return user.getUsername() + DEFAULT_PRIVATE_USER_GROUP_NAME_SUFFIX;
	}
	
	@Override
	public UserGroup loadById(Integer id) {
		return getOne(getUsersRestfulApiUrl() + "/group/" + id, UserGroup.class);
	}
	
	@Override
	public List<UserGroup> loadAll() {
		return getList(getUsersRestfulApiUrl() + "/group", UserGroup.class);
	}
	
	@Override
	public UserInGroup findUserInGroup(UserGroup userGroup, final User user) {
		List<UserInGroup> userInGroups = findUsersInGroup(userGroup);
		return (UserInGroup) CollectionUtils.find(userInGroups, new Predicate() {
			public boolean evaluate(Object userInGroup) {
				return ((UserInGroup) userInGroup).getUserId().equals(user.getId());
			}
		});
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<UserInGroup> findUsersInGroup(UserGroup userGroup) {
		List<Map> userGroupRelations = getList(getUsersRestfulApiUrl() + "/group/" + userGroup.getId() + "/users", Map.class);
		List<UserInGroup> result = new ArrayList<UserInGroup>();
		for (Map userGroupRelation : userGroupRelations) {
			UserInGroup userInGroup = new UserInGroup();
			userInGroup.setGroupId(userGroup.getId());
			userInGroup.setUserId(((Double)userGroupRelation.get("userId")).intValue());
			userInGroup.setJoinStatus(UserGroupJoinRequestStatus.fromCode((String) userGroupRelation.get("statusCode")));
			userInGroup.setRole(UserGroupRole.fromCode((String) userGroupRelation.get("roleCode")));
			result.add(userInGroup);
		}
		return result;
	}
	
	@Override
	public List<UserGroup> findAllUserDefinedGroups() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<UserGroup> findAllRelatedUserGroups(User user) {
		return findByUser(user); //TODO search for ancestors/descendants
	}
	
	@Override
	public List<UserGroup> findDescendantGroups(UserGroup group) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public UserInGroup findUserInGroupOrDescendants(UserGroup userGroup, User user) {
		return findUserInGroup(userGroup, user); //TODO
	}

	@Override
	public UserGroup findByName(final String name) {
		@SuppressWarnings("serial")
		HashMap<String,Object> params = new HashMap<String,Object>(){{
			put("name", name);
		}};
		List<UserGroup> list = getList(getUsersRestfulApiUrl() + "/group", params, UserGroup.class);
		return list.isEmpty() ? null : list.get(0);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<UserGroup> findByUser(User user) {
		List<Map> userGroupRelations = getList(getUsersRestfulApiUrl() + "/user/" + user.getId() + "/groups", Map.class);
		List<UserGroup> result = new ArrayList<UserGroup>();
		for (Map userGroupRelation : userGroupRelations) {
			Double groupId = (Double) userGroupRelation.get("groupId");
			UserGroup group = loadById(groupId.intValue());
			result.add(group);
		}
		result.add(getDefaultPublicUserGroup());
		return result;
	}
	
	@Override
	public List<UserGroup> findPublicUserGroups() {
		@SuppressWarnings("serial")
		List<UserGroup> result = getList(getUsersRestfulApiUrl() + "/group", new HashMap<String, Object>(){{
			put("visibility", "PUBLIC");
			put("systemDefined", false);
		}}, UserGroup.class);
		return result;
	}
	
	@Override
	public UserGroup save(UserGroup userGroup, User activeUser) {
		Integer id = userGroup.getId();
		if (id == null) {
			return post(getUsersRestfulApiUrl() + "/group", userGroup, UserGroup.class);
		} else {
			return patch(getUsersRestfulApiUrl() + "/group/" + id, userGroup, UserGroup.class);
		}
	}
	
	@Override
	public void delete(UserGroup obj) {
		deleteById(obj.getId());
	}
	
	@Override
	public void deleteById(Integer userGroupId) {
		delete(getUsersRestfulApiUrl() + "/group/" + userGroupId);
	}
	
	@Override
	public void deleteRelation(int userGroupId, int userId) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void deleteAllUserRelations(int userId) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public Map<String, String> getQualifiers(UserGroup group, User user) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public UserGroup findUserGroupByResource(String resourceType, String resourceId) {
		@SuppressWarnings("unchecked")
		Map<String, Object> result = getOne(getUsersRestfulApiUrl() + "/resource/" + resourceType + "/" + resourceId, Map.class);
		if (result == null) {
			return null;
		} else {
			String groupIdStr = (String) result.get("groupId");
			int userGroupId = Integer.parseInt(groupIdStr);
			return loadById(userGroupId);
		}
	}
	
	@Override
	public List<String> findResourcesByUserGroup(int userGroupId, String resourceType) {
		return getList(getUsersRestfulApiUrl() + "/group" + userGroupId + "/resource/" + resourceType, String.class);
	}
	
	@Override
	public void associateResource(int userGroupId, String resourceType, String resourceId) {
		String url = getUsersRestfulApiUrl() + "/group" + userGroupId + "/resource/" + resourceType + "/" + resourceId;
		post(url, null, Boolean.class);
	}
	
	@Override
	public void disassociateResource(int userGroupId, String resourceType, String resourceId) {
		String url = getUsersRestfulApiUrl() + "/group" + userGroupId + "/resource/" + resourceType + "/" + resourceId;
		super.delete(url);
	}

	@Override
	public UserGroup createDefaultPrivateUserGroup(User user, User createdByUser) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void joinToDefaultPublicGroup(User user, UserGroupRole role) {
		// TODO Auto-generated method stub
	}
}
