package org.openforis.idm.model.expression.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class NodePropertyHandler implements DynamicPropertyHandler {

	private static final String[] FIXED_PROPERTY_NAMES = new String[]{
		Path.NORMALIZED_PARENT_FUNCTION
	};

	@Override
	public Object getProperty(Object object, String propertyName) {
		if (propertyName.equals(Path.NORMALIZED_PARENT_FUNCTION)) {
			return ((Node<?>) object).getParent();
		} else if (object instanceof Entity) {
			return extractNonEmptyChildren((Entity) object, propertyName);
		} else if ( object instanceof Attribute ) {
			return getAttributeProperty((Attribute<?, ?>) object, propertyName);
		} else {
			return null;
		}
	}

	@Override
	public String[] getPropertyNames(Object object) {
		if (object instanceof Entity) {
			EntityDefinition def = ((Entity) object).getDefinition();
			String[] childDefNames = def.getChildDefinitionNames();
			return ArrayUtils.addAll(childDefNames, FIXED_PROPERTY_NAMES);
		} else {
			return FIXED_PROPERTY_NAMES;
		}
	}

	@Override
	public void setProperty(Object object, String propertyName, Object value) {
		throw new UnsupportedOperationException("setProperty() not supported in " + this.getClass().getSimpleName());
	}

	private Object getAttributeProperty(Attribute<?, ?> attr, String propertyName) {
		Field<?> field = attr.getField(propertyName);
		if (field == null) {
			try {
				Object prop = PropertyUtils.getProperty(attr, propertyName);
				return prop;
			} catch (Exception e) {
				return null;
			}
		} else {
			return field.getValue();
		}
	}

	private Object extractNonEmptyChildren(Entity entity, String childName) {
		NodeDefinition childDef = entity.getDefinition().getChildDefinition(childName);
		List<Node<?>> children = entity.getChildren(childDef);

		List<Node<?>> list = new ArrayList<Node<?>>(children.size());
		
		for (Node<?> childNode : children) {
			if( childNode instanceof Entity || ( ! childNode.isEmpty()) ) {
				list.add(childNode);
			}
		}
		if (list.isEmpty()) {
			return null;
		} else {
			return list;
		}
	}

}