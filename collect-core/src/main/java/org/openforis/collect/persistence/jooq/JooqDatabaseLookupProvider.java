package org.openforis.collect.persistence.jooq;

import org.openforis.collect.model.NameValueEntry;
import org.openforis.collect.persistence.DatabaseLookupProvider;
import org.openforis.collect.persistence.DynamicTableDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
public class JooqDatabaseLookupProvider extends DatabaseLookupProvider {
	
	@Autowired
	private DynamicTableDao dynamicTableDao;

	@Override
	protected Object loadValue(String name, String attribute, NameValueEntry[] filters) {
		Object object = dynamicTableDao.loadValue(name, attribute, filters);
		return object;
	}

	
}