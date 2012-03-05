/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
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
	public Map<String, Boolean> getChildrenRelevanceMap(){
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		for (NodeDefinition childDefinition : childDefinitions) {
			String childName = childDefinition.getName();
			boolean relevant = entity.isRelevant(childName );
			map.put(childName, relevant);
		}
		return map;
	}

	@ExternalizedProperty
	public Map<String, Boolean> getChildrenRequiredMap(){
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		for (NodeDefinition childDefinition : childDefinitions) {
			String childName = childDefinition.getName();
			boolean required = entity.isRequired(childName );
			map.put(childName, required);
		}
		return map;
	}
	
	@ExternalizedProperty
	public Map<String, ValidationResultFlag> getChildrenMinCountValidityMap(){
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		Map<String, ValidationResultFlag> map = new HashMap<String, ValidationResultFlag>();
		for (NodeDefinition childDefinition : childDefinitions) {
			String childName = childDefinition.getName();
			ValidationResultFlag valid = entity.validateMinCount(childName);
			map.put(childName, valid);
		}
		return map;
	}

	@ExternalizedProperty
	public Map<String, ValidationResultFlag> getChildrenMaxCountValidityMap(){
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		Map<String, ValidationResultFlag> map = new HashMap<String, ValidationResultFlag>();
		for (NodeDefinition childDefinition : childDefinitions) {
			String childName = childDefinition.getName();
			ValidationResultFlag valid = entity.validateMaxCount(childName);
			map.put(childName, valid);
		}
		return map;
	}

	private List<NodeDefinition> getChildDefinitions() {
		EntityDefinition definition = entity.getDefinition();
		return definition.getChildDefinitions();
	}
	
}
