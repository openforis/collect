package org.openforis.collect.manager;

import java.util.List;

import static org.openforis.collect.config.CollectConfiguration.*;
import org.openforis.collect.client.AbstractClient;
import org.openforis.collect.model.Institution;

public class InstitutionManager extends AbstractClient {

	public List<Institution> loadAll() {
		List<Institution> result = getList(getUsersRestfulApiUrl(), Institution.class);
		return result;
	}
	
	
	
}
