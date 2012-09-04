/**
 * 
 */
package org.openforis.collect.persistence;

import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class DatabaseExternalCodeListProvider implements ExternalCodeListProvider {
	@Autowired
	private DynamicTableDao dynamicTableDao;

	@Override
	public String getCode(String listName, String attribute, Object... keys) {
		Object object = dynamicTableDao.load(listName, attribute, keys);
		if (object != null) {
			return object.toString();
		}
		return null;
	}

}
