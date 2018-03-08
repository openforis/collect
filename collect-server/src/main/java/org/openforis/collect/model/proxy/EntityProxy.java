/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectSurvey;
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
	private transient List<NodeDefinition> availableChildDefinitions;
	
	public EntityProxy(EntityProxy parent, Entity entity, ProxyContext context) {
		super(parent, entity, context);
		this.entity = entity;
		this.availableChildDefinitions = getAvailableChildDefinitions();
	}

	@ExternalizedProperty
	public Map<Integer, List<NodeProxy>> getChildrenByDefinitionId() {
		Map<Integer, List<NodeProxy>> result = new HashMap<Integer, List<NodeProxy>>();
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			List<Node<?>> nodes = this.entity.getChildren(childDefinition);
			List<NodeProxy> proxies = NodeProxy.fromList(this, nodes, context);
			result.put(childDefinition.getId(), proxies);
		}
		return result;
	}

	@ExternalizedProperty
	public List<Boolean> getChildrenRelevance() {
		List<Boolean> result = new ArrayList<Boolean>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			boolean relevant = entity.isRelevant(childDefinition);
			result.add(relevant);
		}
		return result;
	}

	@ExternalizedProperty
	public List<ValidationResultFlag> getChildrenMinCountValidation() {
		List<ValidationResultFlag> result = new ArrayList<ValidationResultFlag>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			ValidationResultFlag valid = entity.getMinCountValidationResult(childDefinition);
			result.add(valid);
		}
		return result;
	}

	@ExternalizedProperty
	public List<ValidationResultFlag> getChildrenMaxCountValidation() {
		List<ValidationResultFlag> result = new ArrayList<ValidationResultFlag>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			ValidationResultFlag valid = entity.getMaxCountValidationResult(childDefinition);
			result.add(valid);
		}
		return result;
	}
	
	@ExternalizedProperty
	public List<Integer> getChildrenMinCount() {
		List<Integer> result = new ArrayList<Integer>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			int count = entity.getMinCount(childDefinition);
			result.add(count);
		}
		return result;
	}

	@ExternalizedProperty
	public List<Integer> getChildrenMaxCount() {
		List<Integer> result = new ArrayList<Integer>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			Integer count = entity.getMaxCount(childDefinition);
			result.add(count);
		}
		return result;
	}

	@ExternalizedProperty
	public List<Boolean> getChildrenErrorVisible() {
		List<Boolean> result = new ArrayList<Boolean>(availableChildDefinitions.size());
		for (int i = 0; i < availableChildDefinitions.size(); i++) {
			result.add(Boolean.FALSE);
		}
		return result;
	}

	private List<NodeDefinition> getAvailableChildDefinitions() {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		UIOptions uiOptions = ((CollectSurvey) entity.getSurvey()).getUIOptions();
		for (NodeDefinition childDefinition : getChildDefinitions()) {
			if ( isApplicable(childDefinition) && ! uiOptions.isHidden(childDefinition) ) {
				result.add(childDefinition);
			}
		}
		return result;
	}
	
	protected boolean isApplicable(NodeDefinition childDefinition) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		return version == null || version.isApplicable(childDefinition);
	}
	
	@ExternalizedProperty
	public boolean isEnumerated() {
		EntityDefinition definition = entity.getDefinition();
		return definition.isEnumerable() && definition.isEnumerate();
	}
	
	private List<NodeDefinition> getChildDefinitions() {
		EntityDefinition definition = entity.getDefinition();
		return definition.getChildDefinitions();
	}
	
}
