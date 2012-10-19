/**
 * 
 */
package org.openforis.collect.persistence;

import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class DatabaseLookupProvider implements LookupProvider {

	@Autowired
	private DynamicTableDao dynamicTableDao;

	public DatabaseLookupProvider() {
	}

	@Override
	public Object lookup(String name, String attribute, Object... columns) {
		Object object = dynamicTableDao.load(name, attribute, columns);
		if(object != null){
			Coordinate coordinate = Coordinate.parseCoordinate(object.toString());
			return coordinate;
		}
		return null;
	}

}
