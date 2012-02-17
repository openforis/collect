/**
 * 
 */
package org.openforis.collect.manager;

import org.openforis.collect.persistence.ExternalLookupProviderDAO;
import org.openforis.idm.metamodel.validation.ExternalLookupProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class ExternalLookupProviderManager implements ExternalLookupProvider {

	@Autowired
	private ExternalLookupProviderDAO externalLookupProviderDAO;

	public ExternalLookupProviderManager() {
	}

	@Override
	public Object lookup(String name, String attribute, Object... keys) {
		return externalLookupProviderDAO.load(name, attribute, keys);
	}

}
