/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * 
 */
public class EntityProxy extends NodeProxy {

	private transient Entity entity;

	public EntityProxy(Entity entity) {
		super(entity);
		this.entity = entity;
	}
	
	@ExternalizedProperty
	public Map<String, List<NodeProxy>> getChildrenByName() {
		Map<String, List<NodeProxy>> result = new HashMap<String, List<NodeProxy>>();
		EntityDefinition definition = this.entity.getDefinition();
		List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
		for (NodeDefinition childDefinition : childDefinitions) {
			String name = childDefinition.getName();
			List<Node<?>> childrenByName = this.entity.getAll(name);
			if(childrenByName != null) {
				List<NodeProxy> childrenByNameProxies = new ArrayList<NodeProxy>();
				for (Node<?> childNode : childrenByName) {
					if(childNode instanceof Attribute) {
						NodeProxy attributeProxy = new AttributeProxy((Attribute<?, ?>) childNode);
						childrenByNameProxies.add(attributeProxy);
					} else if(childNode instanceof Entity) {
						EntityProxy entityProxy = new EntityProxy((Entity) childNode);
						childrenByNameProxies.add(entityProxy);
					}
				}
				result.put(name, childrenByNameProxies);
			}
		}
		return result;
	}
	
	@ExternalizedProperty
	public List<NodeProxy> getChildren() {
		List<NodeProxy> result = new ArrayList<NodeProxy>();
		Collection<List<NodeProxy>> values = getChildrenByName().values();
		for (List<NodeProxy> list : values) {
			result.addAll(list);
		}
		return result;
	}
	
}
