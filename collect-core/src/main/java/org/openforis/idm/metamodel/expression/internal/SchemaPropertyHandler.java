/**
 * 
 */
package org.openforis.idm.metamodel.expression.internal;

import java.util.List;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * @author M. Togna
 * 
 */
public class SchemaPropertyHandler implements DynamicPropertyHandler {

	@Override
	public String[] getPropertyNames(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getProperty(Object object, String propertyName) {
		if (object instanceof Schema) {
			Schema schema = (Schema) object;
			List<EntityDefinition> entityDefinitions = schema.getRootEntityDefinitions();
			for (EntityDefinition entityDefinition : entityDefinitions) {
				if (propertyName.equals(entityDefinition.getName())) {
					return entityDefinition;
				}
			}

		}
		return null;
	}

	@Override
	public void setProperty(Object object, String propertyName, Object value) {
		// TODO Auto-generated method stub

	}

}
