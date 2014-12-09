/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
	
	public EntityProxy(EntityProxy parent, Entity entity, Locale locale) {
		super(parent, entity, locale);
		this.entity = entity;
	}
	

	@ExternalizedProperty
	public Map<Integer, List<NodeProxy>> getChildrenByDefinitionId() {
		Map<Integer, List<NodeProxy>> result = new HashMap<Integer, List<NodeProxy>>();
		List<NodeDefinition> childDefinitions = getChildDefinitionsInVersion();
		for (NodeDefinition childDefinition : childDefinitions) {
			List<Node<?>> nodes = this.entity.getAll(childDefinition);
			List<NodeProxy> proxies = NodeProxy.fromList(this, nodes, getLocale());
			result.put(childDefinition.getId(), proxies);
		}
		return result;
	}

	@ExternalizedProperty
	public List<Boolean> getChildrenRelevance() {
		List<NodeDefinition> childDefinitions = getChildDefinitionsInVersion();
		List<Boolean> result = new ArrayList<Boolean>(childDefinitions.size());
		for (NodeDefinition childDefinition : childDefinitions) {
			boolean relevant = entity.isRelevant(childDefinition);
			result.add(relevant);
		}
		return result;
	}

	@ExternalizedProperty
	public List<ValidationResultFlag> getChildrenMinCountValidation() {
		List<NodeDefinition> childDefinitions = getChildDefinitionsInVersion();
		List<ValidationResultFlag> result = new ArrayList<ValidationResultFlag>(childDefinitions.size());
		for (NodeDefinition childDefinition : childDefinitions) {
			ValidationResultFlag valid = entity.getMinCountValidationResult(childDefinition);
			result.add(valid);
		}
		return result;
	}

	@ExternalizedProperty
	public List<ValidationResultFlag> getChildrenMaxCountValidation() {
		List<NodeDefinition> childDefinitions = getChildDefinitionsInVersion();
		List<ValidationResultFlag> result = new ArrayList<ValidationResultFlag>(childDefinitions.size());
		for (NodeDefinition childDefinition : childDefinitions) {
			ValidationResultFlag valid = entity.getMaxCountValidationResult(childDefinition);
			result.add(valid);
		}
		return result;
	}
	
	@ExternalizedProperty
	public List<Integer> getChildrenMinCount() {
		List<NodeDefinition> childDefinitions = getChildDefinitionsInVersion();
		List<Integer> result = new ArrayList<Integer>(childDefinitions.size());
		for (NodeDefinition childDefinition : childDefinitions) {
			int count = entity.getMinCount(childDefinition);
			result.add(count);
		}
		return result;
	}

	@ExternalizedProperty
	public List<Integer> getChildrenMaxCount() {
		List<NodeDefinition> childDefinitions = getChildDefinitionsInVersion();
		List<Integer> result = new ArrayList<Integer>(childDefinitions.size());
		for (NodeDefinition childDefinition : getChildDefinitionsInVersion()) {
			Integer count = entity.getMaxCount(childDefinition);
			result.add(count);
		}
		return result;
	}

	@ExternalizedProperty
	public List<Boolean> getChildrenErrorVisible() {
		List<NodeDefinition> childDefinitions = getChildDefinitionsInVersion();
		List<Boolean> result = new ArrayList<Boolean>(childDefinitions.size());
		Collections.fill(result, Boolean.FALSE);
		return result;
	}

	private List<NodeDefinition> getChildDefinitionsInVersion() {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		for (NodeDefinition childDefinition : getChildDefinitions()) {
			if ( isAppliable(childDefinition) ) {
				result.add(childDefinition);
			}
		}
		return result;
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
