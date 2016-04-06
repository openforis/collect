package org.openforis.collect.persistence.jooq;

import org.openforis.collect.model.NameValueEntry;
import org.openforis.collect.persistence.DatabaseLookupProvider;
import org.openforis.collect.persistence.DynamicTableDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class JooqDatabaseLookupProvider extends DatabaseLookupProvider {
	
	@Autowired
	private DynamicTableDao dynamicTableDao;

	@Override
	protected Object loadValue(String name, String attribute, NameValueEntry[] filters) {
		Object object = dynamicTableDao.loadValue(name, attribute, filters);
		return object;
	}

	
}