package org.openforis.idm.model.expression.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class NodePropertyHandler implements DynamicPropertyHandler {

	@Override
	public Object getProperty(Object object, String propertyName) {
		if (propertyName.equals(Path.NORMALIZED_PARENT_FUNCTION)) {
			return ((Node<?>) object).getParent();
		} else if (object instanceof Entity) {
			return extractNonEmptyChildren(propertyName, (Entity) object);
		} else if ( object instanceof Attribute ) {
			return object;
		} else {
			return null;
		}
	}

	@Override
	public String[] getPropertyNames(Object object) {
		List<String> result = new ArrayList<String>();
		if (object instanceof Entity) {
			EntityDefinition def = ((Entity) object).getDefinition();
			result.addAll(def.getChildDefinitionNames());
		}
		result.add(Path.NORMALIZED_PARENT_FUNCTION);
		return result.toArray(new String[result.size()]);
	}

	@Override
	public void setProperty(Object object, String propertyName, Object value) {
		throw new UnsupportedOperationException("setProperty() not supported in " + this.getClass().getSimpleName());
	}

	private Object extractNonEmptyChildren(String propertyName, Entity entity) {
		List<Node<?>> children = entity.getAll(propertyName);
		List<Node<?>> list = new ArrayList<Node<?>>(children.size());
		
		for (Node<?> childNode : children) {
			if( childNode instanceof Entity || ( ! childNode.isEmpty()) ) {
				list.add(childNode);
			}
		}
		if (list.isEmpty()) {
			return null;
		}
		return Collections.unmodifiableList(list);
	}

}