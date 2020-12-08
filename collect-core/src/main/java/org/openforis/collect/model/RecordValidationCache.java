package org.openforis.collect.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordValidationCache {
	private Map<Integer, Set<NodeDefinition>> minCountErrorNodeDefinitionsByEntityId;
	private Map<Integer, Set<NodeDefinition>> minCountWarningNodeDefinitionsByEntityId;
	private Map<Integer, Set<NodeDefinition>> maxCountErrorNodeDefinitionsByEntityId;
	private Map<Integer, Set<NodeDefinition>> maxCountWarningNodeDefinitionsByEntityId;
	private Map<Integer, Integer> errorCountByAttributeId;
	private Map<Integer, Integer> warningCountByAttributeId;
	private Map<Integer, ValidationResults> validationResultsByAttributeId;
	private Set<Integer> skippedNodeIds;
	private CollectRecord record;

	public RecordValidationCache(CollectRecord record) {
		this.record = record;
		this.minCountErrorNodeDefinitionsByEntityId = new HashMap<Integer, Set<NodeDefinition>>();
		this.minCountWarningNodeDefinitionsByEntityId = new HashMap<Integer, Set<NodeDefinition>>();
		this.maxCountErrorNodeDefinitionsByEntityId = new HashMap<Integer, Set<NodeDefinition>>();
		this.maxCountWarningNodeDefinitionsByEntityId = new HashMap<Integer, Set<NodeDefinition>>();
		this.errorCountByAttributeId = new HashMap<Integer, Integer>();
		this.warningCountByAttributeId = new HashMap<Integer, Integer>();
		this.validationResultsByAttributeId = new HashMap<Integer, ValidationResults>();
		this.skippedNodeIds = new HashSet<Integer>();
	}
	
	public Set<Integer> getSkippedNodeIds() {
		return skippedNodeIds;
	}

	public int getTotalMinCountWarnings() {
		return countTotalValues(minCountWarningNodeDefinitionsByEntityId);
	}

	public int getTotalMissingMinCountErrors() {
		return getTotalMissingCount(minCountErrorNodeDefinitionsByEntityId);
	}

	public int getTotalMissingMinCountWarnings() {
		return getTotalMissingCount(minCountWarningNodeDefinitionsByEntityId);
	}

	public int getTotalAttributeErrors() {
		return getAttributeValidationCount(errorCountByAttributeId);
	}

	public int getTotalAttributeWarnings() {
		return getAttributeValidationCount(warningCountByAttributeId);
	}

	public int getTotalMaxCountErrors() {
		return countTotalValues(maxCountErrorNodeDefinitionsByEntityId);
	}

	public int getTotalMaxCountWarnings() {
		return countTotalValues(maxCountWarningNodeDefinitionsByEntityId);
	}

	public void updateMinCountInfo(Integer entityId, NodeDefinition childDef,
			ValidationResultFlag flag) {
		removeMinCountError(entityId, childDef);
		removeMinCountWarning(entityId, childDef);
		switch (flag) {
		case ERROR:
			addMinCountError(entityId, childDef);
			break;
		case WARNING:
			addMinCountWarning(entityId, childDef);
			break;
		default:
		}
	}
	
	public void updateMaxCountInfo(Integer entityId, NodeDefinition childDef,
			ValidationResultFlag flag) {
		removeMaxCountError(entityId, childDef);
		removeMaxCountWarning(entityId, childDef);
		switch (flag) {
		case ERROR:
			addMaxCountError(entityId, childDef);
			break;
		case WARNING:
			addMaxCountWarning(entityId, childDef);
			break;
		default:
		}
	}

	public void addMinCountError(Integer entityId, NodeDefinition childDef) {
		addValueToEntityCache(minCountErrorNodeDefinitionsByEntityId, entityId, childDef);
	}
	
	public void removeMinCountError(Integer entityId, NodeDefinition childDef) {
		removeChildDefinitionFromEntityCache(minCountErrorNodeDefinitionsByEntityId, entityId, childDef);
	}
	
	public void addMinCountWarning(Integer entityId, NodeDefinition childDef) {
		addValueToEntityCache(minCountWarningNodeDefinitionsByEntityId, entityId, childDef);
	}
	
	public void removeMinCountWarning(Integer entityId, NodeDefinition childDef) {
		removeChildDefinitionFromEntityCache(minCountWarningNodeDefinitionsByEntityId, entityId, childDef);
	}
	
	public void addMaxCountError(Integer entityId, NodeDefinition childDef) {
		addValueToEntityCache(maxCountErrorNodeDefinitionsByEntityId, entityId, childDef);
	}
	
	public void removeMaxCountError(int entityId, NodeDefinition childDef) {
		removeChildDefinitionFromEntityCache(maxCountErrorNodeDefinitionsByEntityId, entityId, childDef);
	}

	public void addMaxCountWarning(Integer entityId, NodeDefinition childDef) {
		addValueToEntityCache(maxCountWarningNodeDefinitionsByEntityId, entityId, childDef);
	}
	
	public void removeMaxCountWarning(int entityId, NodeDefinition childDef) {
		removeChildDefinitionFromEntityCache(maxCountWarningNodeDefinitionsByEntityId, entityId, childDef);
	}

	public void setAttributeErrorCount(Integer attributeId, int errorCounts) {
		this.errorCountByAttributeId.put(attributeId, errorCounts);
	}

	public void setAttributeWarningCount(int attributeId, int warningCounts) {
		this.warningCountByAttributeId.put(attributeId, warningCounts);
	}

	public void setAttributeValidationResults(Integer attributeId,
			ValidationResults validationResults) {
		this.validationResultsByAttributeId.put(attributeId, validationResults);		
	}
	
	public ValidationResults getAttributeValidationResults(int attributeId) {
		return validationResultsByAttributeId.get(attributeId);
	}
	
	public Map<Integer, ValidationResults> getValidationResultsByAttributeId() {
		return CollectionUtils.unmodifiableMap(validationResultsByAttributeId);
	}
	
	public Set<NodeDefinition> getMinCountErrorChildDefinitions(int entityId) {
		return CollectionUtils.unmodifiableSet(minCountErrorNodeDefinitionsByEntityId.get(entityId));
	}
	
	public Set<NodeDefinition> getMaxCountErrorChildDefinitions(int entityId) {
		return CollectionUtils.unmodifiableSet(maxCountErrorNodeDefinitionsByEntityId.get(entityId));
	}
	
	public Set<NodeDefinition> getMinCountWarningChildDefinitions(int entityId) {
		return CollectionUtils.unmodifiableSet(minCountWarningNodeDefinitionsByEntityId.get(entityId));
	}
	
	public Set<NodeDefinition> getMaxCountWarningChildDefinitions(int entityId) {
		return CollectionUtils.unmodifiableSet(maxCountWarningNodeDefinitionsByEntityId.get(entityId));
	}
	
	public Set<NodeDefinition> getCardinalityFailedChildDefinitions(int entityId, ValidationResultFlag severity, boolean minCount) {
		if ( minCount ) {
			switch(severity) {
			case ERROR:
				return getMinCountErrorChildDefinitions(entityId);
			case WARNING:
				return getMinCountWarningChildDefinitions(entityId);
			default:
				return Collections.emptySet();
			}
		} else {
			switch(severity) {
			case ERROR:
				return getMaxCountErrorChildDefinitions(entityId);
			case WARNING:
				return getMaxCountWarningChildDefinitions(entityId);
			default:
				return Collections.emptySet();
			}
		}
	}

	public void remove(Node<?> node) {
		Integer nodeId = node.getInternalId();
		if(node instanceof Attribute<?, ?>) {
			skippedNodeIds.remove(nodeId);
			errorCountByAttributeId.remove(nodeId);
			warningCountByAttributeId.remove(nodeId);
			validationResultsByAttributeId.remove(nodeId);
		} else {
			minCountErrorNodeDefinitionsByEntityId.remove(nodeId);
			maxCountErrorNodeDefinitionsByEntityId.remove(nodeId);
			minCountWarningNodeDefinitionsByEntityId.remove(nodeId);
			maxCountWarningNodeDefinitionsByEntityId.remove(nodeId);
		}
	}

	public void addSkippedNodeId(Integer attributeId) {
		skippedNodeIds.add(attributeId);		
	}

	protected Set<NodeDefinition> removeChildDefinitionFromEntityCache(Map<Integer, Set<NodeDefinition>> childDefinitionsByEntityId, int entityId, NodeDefinition childDef) {
		Set<NodeDefinition> set = childDefinitionsByEntityId.get(entityId);
		if(set != null) {
			set.remove(childDef);
		}
		return set;
	}
	
	protected void addValueToEntityCache(Map<Integer, Set<NodeDefinition>> childDefinitionsByEntityId, int entityId, NodeDefinition childDef) {
		Set<NodeDefinition> set = childDefinitionsByEntityId.get(entityId);
		if ( set == null ) {
			set = new HashSet<NodeDefinition>();
			childDefinitionsByEntityId.put(entityId, set);
		}
		set.add(childDef);
	}

	protected int countTotalValues(Map<Integer, Set<NodeDefinition>> map) {
		int count = 0;
		for (Set<NodeDefinition> set : map.values()) {
			count += set.size();
		}
		return count;
	}
	
	protected int getTotalMissingCount(Map<Integer, Set<NodeDefinition>> nodeDefsByEntityId) {
		int result = 0;
		Set<Integer> keySet = nodeDefsByEntityId.keySet();
		for (Integer id : keySet) {
			Entity entity = (Entity) record.getNodeByInternalId(id);
			Set<NodeDefinition> nodeDefs = nodeDefsByEntityId.get(id);
			for (NodeDefinition childDef : nodeDefs) {
				int missingCount = entity.getMissingCount(childDef);
				result += missingCount;
			}
		}
		return result;
	}
	
	protected int getAttributeValidationCount(Map<Integer, Integer> map) {
		int count = 0;
		for (Integer i : map.values()) {
			count += i;
		}
		return count;
	}

	
}
