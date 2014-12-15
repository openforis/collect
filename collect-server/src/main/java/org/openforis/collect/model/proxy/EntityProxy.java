/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
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
	private transient List<NodeDefinition> childDefinitionsInVersion;
	
	public EntityProxy(EntityProxy parent, Entity entity, Locale locale) {
		super(parent, entity, locale);
		this.entity = entity;
		this.childDefinitionsInVersion = getChildDefinitionsInVersion();
	}
	

	@ExternalizedProperty
	public Map<Integer, List<NodeProxy>> getChildrenByDefinitionId() {
		Map<Integer, List<NodeProxy>> result = new HashMap<Integer, List<NodeProxy>>();
		for (NodeDefinition childDefinition : childDefinitionsInVersion) {
			List<Node<?>> nodes = this.entity.getAll(childDefinition);
			List<NodeProxy> proxies = NodeProxy.fromList(this, nodes, getLocale());
			result.put(childDefinition.getId(), proxies);
		}
		return result;
	}

	@ExternalizedProperty
	public List<Boolean> getChildrenRelevance() {
		List<Boolean> result = new ArrayList<Boolean>(childDefinitionsInVersion.size());
		for (NodeDefinition childDefinition : childDefinitionsInVersion) {
			boolean relevant = entity.isRelevant(childDefinition);
			result.add(relevant);
		}
		return result;
	}

	@ExternalizedProperty
	public List<ValidationResultFlag> getChildrenMinCountValidation() {
		List<ValidationResultFlag> result = new ArrayList<ValidationResultFlag>(childDefinitionsInVersion.size());
		for (NodeDefinition childDefinition : childDefinitionsInVersion) {
			ValidationResultFlag valid = entity.getMinCountValidationResult(childDefinition);
			result.add(valid);
		}
		return result;
	}

	@ExternalizedProperty
	public List<ValidationResultFlag> getChildrenMaxCountValidation() {
		List<ValidationResultFlag> result = new ArrayList<ValidationResultFlag>(childDefinitionsInVersion.size());
		for (NodeDefinition childDefinition : childDefinitionsInVersion) {
			ValidationResultFlag valid = entity.getMaxCountValidationResult(childDefinition);
			result.add(valid);
		}
		return result;
	}
	
	@ExternalizedProperty
	public List<Integer> getChildrenMinCount() {
		List<Integer> result = new ArrayList<Integer>(childDefinitionsInVersion.size());
		for (NodeDefinition childDefinition : childDefinitionsInVersion) {
			int count = entity.getMinCount(childDefinition);
			result.add(count);
		}
		return result;
	}

	@ExternalizedProperty
	public List<Integer> getChildrenMaxCount() {
		List<Integer> result = new ArrayList<Integer>(childDefinitionsInVersion.size());
		for (NodeDefinition childDefinition : getChildDefinitionsInVersion()) {
			Integer count = entity.getMaxCount(childDefinition);
			result.add(count);
		}
		return result;
	}

	@ExternalizedProperty
	public List<Boolean> getChildrenErrorVisible() {
		List<Boolean> result = new ArrayList<Boolean>(childDefinitionsInVersion.size());
		for (int i = 0; i < childDefinitionsInVersion.size(); i++) {
			result.add(Boolean.FALSE);
		}
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
