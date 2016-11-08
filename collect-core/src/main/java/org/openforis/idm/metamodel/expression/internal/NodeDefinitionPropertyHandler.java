/**
 * 
 */
package org.openforis.idm.metamodel.expression.internal;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.path.Path;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class NodeDefinitionPropertyHandler implements DynamicPropertyHandler {

	private static final String[] FIXED_PROPERTY_NAMES = new String[] {
			Path.NORMALIZED_PARENT_FUNCTION
	};

	@Override
	public String[] getPropertyNames(Object object) {
		if (object instanceof EntityDefinition) {
			String[] childDefNames = ((EntityDefinition) object).getChildDefinitionNames();
			return ArrayUtils.addAll(childDefNames, FIXED_PROPERTY_NAMES);
		} else {
			return FIXED_PROPERTY_NAMES;
		}
	}

	@Override
	public Object getProperty(Object object, String propertyName) {
		if (propertyName.equals(Path.NORMALIZED_PARENT_FUNCTION)) {
			NodeDefinition nodeDefinition = (NodeDefinition) object;
			return nodeDefinition.getParentDefinition();
		} else if (object instanceof EntityDefinition) {
			EntityDefinition entityDefinition = (EntityDefinition) object;
			return entityDefinition.getChildDefinition(propertyName);
		} else if (object instanceof AttributeDefinition) {
			AttributeDefinition attrDefn = (AttributeDefinition) object;
			if (attrDefn.hasField(propertyName)) {
				return attrDefn.getFieldDefinition(propertyName);
			}
		}
		return null;
	}

	@Override
	public void setProperty(Object object, String propertyName, Object value) {
		throw new UnsupportedOperationException("setProperty() not supported in " + this.getClass().getSimpleName());
	}

}