package org.openforis.collect.manager;

import static org.openforis.collect.config.CollectConfiguration.getUsersRestfulApiUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.openforis.collect.client.AbstractClient;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserGroup.UserInGroup;
import org.openforis.collect.model.User;

public class ClientUserGroupManager extends AbstractClient implements UserGroupManager {

	@Override
	public UserGroup getDefaultPublicUserGroup() {
		return findByName(DEFAULT_PUBLIC_USER_GROUP_NAME);
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
	public List<UserInGroup> findUsersByGroup(UserGroup userGroup) {
		// TODO Auto-generated method stub
		return null;
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<UserGroup> findByUser(User user) {
		List<UserGroup> result = new ArrayList<UserGroup>();
		List<Map> userGroups = getList(getUsersRestfulApiUrl() + "/user/" + user.getId() + "/groups", Map.class);
		for (Map<String, Object> item : userGroups) {
			Object group = item.get("group");
			UserGroup userGroup = new UserGroup();
			try {
				BeanUtils.copyProperties(userGroup, group);
				result.add(userGroup);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}
	
	@Override
	public List<UserGroup> findPublicUserGroups() {
		@SuppressWarnings("serial")
		List<UserGroup> result = getList(getUsersRestfulApiUrl() + "/group", new HashMap<String, Object>(){{
			put("visibility", "PUBLIC");
		}}, UserGroup.class);
		return result;
	}
	
	@Override
	public List<UserGroup> findUserDefinedGroups() {
		@SuppressWarnings("serial")
		List<UserGroup> result = getList(getUsersRestfulApiUrl() + "/group", new HashMap<String, Object>(){{
			put("visibility", "PUBLIC");
			put("systemDefined", "false");
		}}, UserGroup.class);
		return result;
	}
	
	@Override
	public UserGroup save(UserGroup userGroup) {
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

}
