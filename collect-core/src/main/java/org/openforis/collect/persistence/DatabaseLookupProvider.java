/**
 * 
 */
package org.openforis.collect.persistence;

import org.openforis.idm.metamodel.validation.LookupProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class DatabaseLookupProvider implements LookupProvider {

	@Autowired
	private LookupProviderDao lookupProviderDao;

	public DatabaseLookupProvider() {
	}

	@Override
	public Object lookup(String name, String attribute, Object... keys) {
		return lookupProviderDao.load(name, attribute, keys);
	}

}
