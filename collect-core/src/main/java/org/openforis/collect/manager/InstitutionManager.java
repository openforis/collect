package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.Institution;

public interface InstitutionManager {

	List<Institution> loadAll();

	List<Institution> loadPublicInstitutions();

	Institution loadById(long id);
	
	Institution save(Institution institution);

	void delete(long institutionId);

	Institution findInstitutionByResource(String resourceType, String resourceId);
	
	List<String> findResourcesByInstitution(long institutionId, String resourceType);
	
	void associateResource(long institutionId, String resourceType, String resourceId);
	
	void disassociateResource(long institutionId, String resourceType, String resourceId);
}