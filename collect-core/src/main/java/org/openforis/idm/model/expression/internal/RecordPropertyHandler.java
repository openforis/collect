/**
 * 
 */
package org.openforis.idm.model.expression.internal;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Record;

/**
 * @author M. Togna
 * 
 */
public class RecordPropertyHandler implements DynamicPropertyHandler {

	@Override
	public Object getProperty(Object object, String propertyName) {
		if (object instanceof Record) {
			Entity entity = ((Record) object).getRootEntity();
			if (entity.getName().equals(propertyName)) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public String[] getPropertyNames(Object object) {
		return null;
	}

	@Override
	public void setProperty(Object object, String propertyName, Object value) {
		// TODO Auto-generated method stub

	}

}
