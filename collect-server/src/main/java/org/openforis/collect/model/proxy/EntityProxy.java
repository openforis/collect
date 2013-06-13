/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class EntityProxy extends NodeProxy {

	private transient Entity entity;
	
	public EntityProxy(EntityProxy parent, Entity entity) {
		super(parent, entity);
		this.entity = entity;
	}
	
	@ExternalizedProperty
	public Map<String, List<NodeProxy>> getChildrenByName() {
		Map<String, List<NodeProxy>> result = new HashMap<String, List<NodeProxy>>();
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		for (NodeDefinition childDefinition : childDefinitions) {
			if ( isAppliable(childDefinition) ) {
				String name = childDefinition.getName();
				List<Node<?>> childrenByName = this.entity.getAll(name);
				List<NodeProxy> proxies = NodeProxy.fromList(this, childrenByName);
				result.put(name, proxies);
			}
		}
		return result;
	}

	@ExternalizedProperty
	public Map<String, Boolean> getChildrenRelevanceMap(){
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		for (NodeDefinition childDefinition : childDefinitions) {
			if ( isAppliable(childDefinition) ) {
				String childName = childDefinition.getName();
				boolean relevant = entity.isRelevant(childName );
				map.put(childName, relevant);
			}
		}
		return map;
	}

	@ExternalizedProperty
	public Map<String, Boolean> getChildrenRequiredMap(){
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		for (NodeDefinition childDefinition : childDefinitions) {
			if ( isAppliable(childDefinition) ) {
				String childName = childDefinition.getName();
				boolean required = entity.isRequired(childName );
				map.put(childName, required);
			}
		}
		return map;
	}
	
	@ExternalizedProperty
	public Map<String, ValidationResultFlag> getChildrenMinCountValidationMap(){
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		Map<String, ValidationResultFlag> map = new HashMap<String, ValidationResultFlag>();
		for (NodeDefinition childDefinition : childDefinitions) {
			if ( isAppliable(childDefinition) ) {
				String childName = childDefinition.getName();
				ValidationResultFlag valid = entity.validateMinCount(childName);
				map.put(childName, valid);
			}
		}
		return map;
	}

	@ExternalizedProperty
	public Map<String, ValidationResultFlag> getChildrenMaxCountValidationMap(){
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		Map<String, ValidationResultFlag> map = new HashMap<String, ValidationResultFlag>();
		for (NodeDefinition childDefinition : childDefinitions) {
			if ( isAppliable(childDefinition) ) {
				String childName = childDefinition.getName();
				ValidationResultFlag valid = entity.validateMaxCount(childName);
				map.put(childName, valid);
			}
		}
		return map;
	}

	@ExternalizedProperty
	public Map<String, Boolean> getShowChildrenErrorsMap() {
		List<NodeDefinition> childDefinitions = getChildDefinitions();
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		for (NodeDefinition childDefinition : childDefinitions) {
			if ( isAppliable(childDefinition) ) {
				String childName = childDefinition.getName();
				map.put(childName, Boolean.FALSE);
			}
		}
		return map;
	}

	protected boolean isAppliable(NodeDefinition childDefinition) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		return version == null || version.isApplicable(childDefinition);
	}
	
	@ExternalizedProperty
	public boolean isEnumerated() {
		EntityDefinition definition = entity.getDefinition();
		return definition.isEnumerable();
	}
	
	private List<NodeDefinition> getChildDefinitions() {
		EntityDefinition definition = entity.getDefinition();
		return definition.getChildDefinitions();
	}
	
}
