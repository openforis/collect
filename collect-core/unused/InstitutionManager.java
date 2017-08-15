package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.model.Institution;
import org.openforis.collect.model.User;

public interface InstitutionManager {
	
	static String DEFAULT_PUBLIC_INSTITUTION_NAME = "default_public_group";
	static String DEFAULT_PRIVATE_INSTITUTION_NAME_SUFFIX = "_private_group";

	String getDefaultPrivateInstitutionName(User user);
	
	Institution findById(long id);
	
	Institution findByName(String name);
	
	List<Institution> findAll();

	List<Institution> findPublicInstitutions();

	List<Institution> findByUser(User user);
	
	Institution save(Institution institution);

	void delete(long institutionId);

	Institution findInstitutionByResource(String resourceType, String resourceId);
	
	List<String> findResourcesByInstitution(long institutionId, String resourceType);
	
	void associateResource(long institutionId, String resourceType, String resourceId);
	
	void disassociateResource(long institutionId, String resourceType, String resourceId);

}