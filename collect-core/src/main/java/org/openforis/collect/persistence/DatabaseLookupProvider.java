/**
 * 
 */
package org.openforis.collect.persistence;

import org.openforis.collect.model.NameValueEntry;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class DatabaseLookupProvider implements LookupProvider {

	@Autowired
	private DynamicTableDao dynamicTableDao;

	@Override
	public Object lookup(String name, String attribute, Object... columns) {
		NameValueEntry[] filters = NameValueEntry.fromKeyValuePairs(columns);
		Object object = dynamicTableDao.loadValue(name, attribute, filters);
		if(object != null){
			Coordinate coordinate = Coordinate.parseCoordinate(object.toString());
			return coordinate;
		}
		return null;
	}

}
