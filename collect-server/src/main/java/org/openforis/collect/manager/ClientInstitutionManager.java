package org.openforis.collect.manager;

import static org.openforis.collect.config.CollectConfiguration.getUsersRestfulApiUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.openforis.collect.client.AbstractClient;
import org.openforis.collect.model.Institution;
import org.openforis.collect.model.User;

public class ClientInstitutionManager extends AbstractClient implements InstitutionManager {

	@Override
	public String getDefaultPrivateInstitutionName(User user) {
		return user.getUsername() + DEFAULT_PRIVATE_INSTITUTION_NAME_SUFFIX;
	}
	
	@Override
	public List<Institution> findAll() {
		return getList(getUsersRestfulApiUrl() + "/group", Institution.class);
	}
	
	@Override
	public Institution findByName(final String name) {
		@SuppressWarnings("serial")
		HashMap<String,Object> params = new HashMap<String,Object>(){{
			put("name", name);
		}};
		List<Institution> list = getList(getUsersRestfulApiUrl() + "/group", params, Institution.class);
		return list.isEmpty() ? null : list.get(0);
	}
	
	@Override
	public Institution findById(long id) {
		return getOne(getUsersRestfulApiUrl() + "/group/" + id, Institution.class);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Institution> findByUser(User user) {
		List<Institution> result = new ArrayList<Institution>();
		List<Map> userGroups = getList(getUsersRestfulApiUrl() + "/user/" + user.getId() + "/groups", Map.class);
		for (Map<String, Object> item : userGroups) {
			Object group = item.get("group");
			Institution institution = new Institution();
			try {
				BeanUtils.copyProperties(institution, group);
				result.add(institution);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}
	
	@Override
	public List<Institution> findPublicInstitutions() {
		@SuppressWarnings("serial")
		List<Institution> result = getList(getUsersRestfulApiUrl() + "/group", new HashMap<String, Object>(){{
			put("visibility", "PUBLIC");
		}}, Institution.class);
		return result;
	}
	
	@Override
	public Institution save(Institution institution) {
		Long id = institution.getId();
		if (id == null) {
			return post(getUsersRestfulApiUrl() + "/group", institution, Institution.class);
		} else {
			return patch(getUsersRestfulApiUrl() + "/group/" + id, institution, Institution.class);
		}
	}
	
	@Override
	public void delete(long institutionId) {
		delete(getUsersRestfulApiUrl() + "/group/" + institutionId);
	}
	
	@Override
	public Institution findInstitutionByResource(String resourceType, String resourceId) {
		@SuppressWarnings("unchecked")
		Map<String, Object> result = getOne(getUsersRestfulApiUrl() + "/resource/" + resourceType + "/" + resourceId, Map.class);
		if (result == null) {
			return null;
		} else {
			Long institutionId = (Long) result.get("groupId");
			return findById(institutionId);
		}
	}
	
	@Override
	public List<String> findResourcesByInstitution(long institutionId, String resourceType) {
		return getList(getUsersRestfulApiUrl() + "/group" + institutionId + "/resource/" + resourceType, String.class);
	}
	
	@Override
	public void associateResource(long institutionId, String resourceType, String resourceId) {
		String url = getUsersRestfulApiUrl() + "/group" + institutionId + "/resource/" + resourceType + "/" + resourceId;
		post(url, null, Boolean.class);
	}
	
	@Override
	public void disassociateResource(long institutionId, String resourceType, String resourceId) {
		String url = getUsersRestfulApiUrl() + "/group" + institutionId + "/resource/" + resourceType + "/" + resourceId;
		super.delete(url);
	}

}
