/**
 * 
 */
package org.openforis.idm.metamodel.expression.internal;

import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.path.Path;

/**
 * @author M. Togna
 * 
 */
public class NodeDefinitionPropertyHandler implements DynamicPropertyHandler {

	@Override
	public String[] getPropertyNames(Object object) {
		String[] array;
		if (object instanceof EntityDefinition) {
			EntityDefinition entityDef = (EntityDefinition) object;
			List<NodeDefinition> childDefs = entityDef.getChildDefinitions();
			array = new String[childDefs.size()+1];
			int i = 0;
			for (NodeDefinition def : childDefs) {
				array[i++] = def.getName();
			}
		} else {
			array = new String[1];
		}
		int last = array.length -1;
		array[last] = Path.NORMALIZED_PARENT_FUNCTION;
		return array;
	}

	@Override
	public Object getProperty(Object object, String propertyName) {
		Object property = null;
		if (propertyName.equals(Path.NORMALIZED_PARENT_FUNCTION)) {
			NodeDefinition nodeDefinition = (NodeDefinition) object;
			property = nodeDefinition.getParentDefinition();
		} else if (object instanceof EntityDefinition) {
			EntityDefinition entityDefinition = (EntityDefinition) object;
			property = entityDefinition.getChildDefinition(propertyName);
		} else if (object instanceof AttributeDefinition) {
			try {
				property = PropertyUtils.getProperty(object, propertyName);
			} catch (Exception e) {
				return null;
			}
		}
		return property;
	}

	@Override
	public void setProperty(Object object, String propertyName, Object value) {
		throw new UnsupportedOperationException("setProperty() not supported in " + this.getClass().getSimpleName());
	}

}