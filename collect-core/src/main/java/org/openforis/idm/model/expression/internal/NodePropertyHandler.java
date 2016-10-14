package org.openforis.idm.model.expression.internal;

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
import org.openforis.idm.model.NodePredicate;
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
		if (attr.getDefinition().hasField(propertyName)) {
			Field<?> field = attr.getField(propertyName);
			return field.getValue();
		} else {
			try {
				Object prop = PropertyUtils.getProperty(attr, propertyName);
				return prop;
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * Returns only non empty children (null if anything is found).
	 * If the children are entities, returns them anyway.
	 * 
	 * @param entity
	 * @param childName
	 * @return
	 */
	private Object extractNonEmptyChildren(Entity entity, String childName) {
		List<? extends Node<?>> list = null;
		NodeDefinition childDef = entity.getDefinition().getChildDefinition(childName);
		if (childDef instanceof EntityDefinition) {
			//return all child entities
			list = entity.getChildren(childDef);
		} else {
			//return only not empty attributes
			list = entity.findChildren(childDef, new NodePredicate() {
				public boolean evaluate(Node<?> childNode) {
					return ! childNode.isEmpty();
				}
			});
		}
		return list.isEmpty() ? null : list;
	}

}