package org.openforis.collect.manager;

import static org.openforis.collect.config.CollectConfiguration.getUsersRestfulApiUrl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.client.AbstractClient;
import org.openforis.collect.model.Institution;
import org.springframework.stereotype.Component;

@Component
public class ClientInstitutionManager extends AbstractClient implements InstitutionManager {

	@Override
	public List<Institution> loadAll() {
		List<Institution> result = getList(getUsersRestfulApiUrl() + "/group", Institution.class);
		return result;
	}
	
	@Override
	public Institution loadById(long id) {
		return getOne(getUsersRestfulApiUrl() + "/group" + id, Institution.class);
	}
	
	@Override
	public List<Institution> loadPublicInstitutions() {
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
			Institution institution = loadById(institutionId);
			return institution;
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
