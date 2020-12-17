/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public Map<Integer, List<NodeProxy>> getChildrenByDefinitionId() {
		Map<Integer, List<NodeProxy>> result = new HashMap<Integer, List<NodeProxy>>();
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			List<Node<?>> nodes = this.entity.getChildren(childDefinition);
			List<NodeProxy> proxies = NodeProxy.fromList(this, nodes, context);
			result.put(childDefinition.getId(), proxies);
		}
		return result;
	}

	public Map<Integer, Boolean> getChildrenRelevanceByDefinitionId() {
		Map<Integer, Boolean> map = new HashMap<Integer, Boolean>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			map.put(childDefinition.getId(), entity.isRelevant(childDefinition));
		}
		return map;
	}

	public Map<Integer, ValidationResultFlag> getChildrenMinCountValidationByDefinitionId() {
		Map<Integer, ValidationResultFlag> map = new HashMap<Integer, ValidationResultFlag>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			map.put(childDefinition.getId(), entity.getMinCountValidationResult(childDefinition));
		}
		return map;
	}
	
	public Map<Integer, ValidationResultFlag> getChildrenMaxCountValidationByDefinitionId() {
		Map<Integer, ValidationResultFlag> map = new HashMap<Integer, ValidationResultFlag>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			map.put(childDefinition.getId(), entity.getMaxCountValidationResult(childDefinition));
		}
		return map;
	}
	
	public Map<Integer, Integer> getChildrenMinCountByDefinitionId() {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			map.put(childDefinition.getId(), entity.getMinCount(childDefinition));
		}
		return map;
	}

	public Map<Integer, Integer> getChildrenMaxCountByDefinitionId() {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			map.put(childDefinition.getId(), entity.getMaxCount(childDefinition));
		}
		return map;
	}

	public Map<Integer, Boolean> getChildrenErrorVisibleByDefinitionId() {
		Map<Integer, Boolean> map = new HashMap<Integer, Boolean>(availableChildDefinitions.size());
		for (NodeDefinition childDefinition : availableChildDefinitions) {
			map.put(childDefinition.getId(), Boolean.FALSE);
		}
		return map;
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
	
	public boolean isEnumerated() {
		EntityDefinition definition = entity.getDefinition();
		return definition.isEnumerable() && definition.isEnumerate();
	}
	
	private List<NodeDefinition> getChildDefinitions() {
		EntityDefinition definition = entity.getDefinition();
		return definition.getChildDefinitions();
	}
	
}
