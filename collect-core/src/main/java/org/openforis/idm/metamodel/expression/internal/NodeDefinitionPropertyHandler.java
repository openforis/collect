/**
 * 
 */
package org.openforis.idm.metamodel.expression.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.jxpath.DynamicPropertyHandler;
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

	@Override
	public String[] getPropertyNames(Object object) {
		List<String> names = new ArrayList<String>();
		if (object instanceof EntityDefinition) {
			names.addAll(((EntityDefinition) object).getChildDefinitionNames());
		}
		names.add(Path.NORMALIZED_PARENT_FUNCTION);
		return names.toArray(new String[names.size()]);
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