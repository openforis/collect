package org.openforis.collect.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openforis.commons.collection.CollectionUtils;
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
	private Map<Integer, Set<String>> minCountErrorChildNamesByEntityId;
	private Map<Integer, Set<String>> minCountWarningChildNamesByEntityId;
	private Map<Integer, Set<String>> maxCountErrorChildNamesByEntityId;
	private Map<Integer, Set<String>> maxCountWarningChildNamesByEntityId;
	private Map<Integer, Integer> errorCountByAttributeId;
	private Map<Integer, Integer> warningCountByAttributeId;
	private Map<Integer, ValidationResults> validationResultsByAttributeId;
	private Set<Integer> skippedNodeIds;
	private CollectRecord record;

	public RecordValidationCache(CollectRecord record) {
		this.record = record;
		this.minCountErrorChildNamesByEntityId = new HashMap<Integer, Set<String>>();
		this.minCountWarningChildNamesByEntityId = new HashMap<Integer, Set<String>>();
		this.maxCountErrorChildNamesByEntityId = new HashMap<Integer, Set<String>>();
		this.maxCountWarningChildNamesByEntityId = new HashMap<Integer, Set<String>>();
		this.errorCountByAttributeId = new HashMap<Integer, Integer>();
		this.warningCountByAttributeId = new HashMap<Integer, Integer>();
		this.validationResultsByAttributeId = new HashMap<Integer, ValidationResults>();
		this.skippedNodeIds = new HashSet<Integer>();
	}
	
	public Set<Integer> getSkippedNodeIds() {
		return skippedNodeIds;
	}

	public int getTotalMinCountWarnings() {
		return countTotalValues(minCountWarningChildNamesByEntityId);
	}

	public int getTotalMissingMinCountErrors() {
		return getTotalMissingCount(minCountErrorChildNamesByEntityId);
	}

	public int getTotalMissingMinCountWarnings() {
		return getTotalMissingCount(minCountWarningChildNamesByEntityId);
	}

	public int getTotalAttributeErrors() {
		return getAttributeValidationCount(errorCountByAttributeId);
	}

	public int getTotalAttributeWarnings() {
		return getAttributeValidationCount(warningCountByAttributeId);
	}

	public int getTotalMaxCountErrors() {
		return countTotalValues(maxCountErrorChildNamesByEntityId);
	}

	public int getTotalMaxCountWarnings() {
		return countTotalValues(maxCountWarningChildNamesByEntityId);
	}

	public void updateMinCountInfo(Integer entityId, String childName,
			ValidationResultFlag flag) {
		removeMinCountError(entityId, childName);
		removeMinCountWarning(entityId, childName);
		switch (flag) {
		case ERROR:
			addMinCountError(entityId, childName);
			break;
		case WARNING:
			addMinCountWarning(entityId, childName);
			break;
		default:
		}
	}
	
	public void updateMaxCountInfo(Integer entityId, String childName,
			ValidationResultFlag flag) {
		removeMaxCountError(entityId, childName);
		removeMaxCountWarning(entityId, childName);
		switch (flag) {
		case ERROR:
			addMaxCountError(entityId, childName);
			break;
		case WARNING:
			addMaxCountWarning(entityId, childName);
			break;
		default:
		}
	}

	public void addMinCountError(Integer entityId, String childName) {
		addValueToEntityCache(minCountErrorChildNamesByEntityId, entityId, childName);
	}
	
	public void removeMinCountError(Integer entityId, String childName) {
		removeChildNameFromEntityCache(minCountErrorChildNamesByEntityId, entityId, childName);
	}
	
	public void addMinCountWarning(Integer entityId, String childName) {
		addValueToEntityCache(minCountWarningChildNamesByEntityId, entityId, childName);
	}
	
	public void removeMinCountWarning(Integer entityId, String childName) {
		removeChildNameFromEntityCache(minCountWarningChildNamesByEntityId, entityId, childName);
	}
	
	public void addMaxCountError(Integer entityId, String childName) {
		addValueToEntityCache(maxCountErrorChildNamesByEntityId, entityId, childName);
	}
	
	public void removeMaxCountError(int entityId, String childName) {
		removeChildNameFromEntityCache(maxCountErrorChildNamesByEntityId, entityId, childName);
	}

	public void addMaxCountWarning(Integer entityId, String childName) {
		addValueToEntityCache(maxCountWarningChildNamesByEntityId, entityId, childName);
	}
	
	public void removeMaxCountWarning(int entityId,
			String childName) {
		removeChildNameFromEntityCache(maxCountWarningChildNamesByEntityId, entityId, childName);
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
	
	public Set<String> getMinCountErrorChildNames(int entityId) {
		return CollectionUtils.unmodifiableSet(minCountErrorChildNamesByEntityId.get(entityId));
	}
	
	public Set<String> getMaxCountErrorChildNames(int entityId) {
		return CollectionUtils.unmodifiableSet(maxCountErrorChildNamesByEntityId.get(entityId));
	}
	
	public Set<String> getMinCountWarningChildNames(int entityId) {
		return CollectionUtils.unmodifiableSet(minCountWarningChildNamesByEntityId.get(entityId));
	}
	
	public Set<String> getMaxCountWarningChildNames(int entityId) {
		return CollectionUtils.unmodifiableSet(maxCountWarningChildNamesByEntityId.get(entityId));
	}
	
	public Set<String> getCardinalityFailedChildNames(int entityId, ValidationResultFlag severity, boolean minCount) {
		if ( minCount ) {
			switch(severity) {
			case ERROR:
				return getMinCountErrorChildNames(entityId);
			case WARNING:
				return getMinCountWarningChildNames(entityId);
			default:
				return Collections.emptySet();
			}
		} else {
			switch(severity) {
			case ERROR:
				return getMaxCountErrorChildNames(entityId);
			case WARNING:
				return getMaxCountWarningChildNames(entityId);
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
			minCountErrorChildNamesByEntityId.remove(nodeId);
			maxCountErrorChildNamesByEntityId.remove(nodeId);
			minCountWarningChildNamesByEntityId.remove(nodeId);
			maxCountWarningChildNamesByEntityId.remove(nodeId);
		}
	}

	public void addSkippedNodeId(Integer attributeId) {
		skippedNodeIds.add(attributeId);		
	}

	protected Set<String> removeChildNameFromEntityCache(Map<Integer, Set<String>> childNamesByEntityId, int entityId, String childName) {
		Set<String> set = childNamesByEntityId.get(entityId);
		if(set != null) {
			set.remove(childName);
		}
		return set;
	}
	
	protected void addValueToEntityCache(Map<Integer, Set<String>> childNamesByEntityId, int entityId, String childName) {
		Set<String> set = childNamesByEntityId.get(entityId);
		if ( set == null ) {
			set = new HashSet<String>();
			childNamesByEntityId.put(entityId, set);
		}
		set.add(childName);
	}

	protected int countTotalValues(Map<Integer, Set<String>> map) {
		int count = 0;
		for (Set<String> set : map.values()) {
			count += set.size();
		}
		return count;
	}
	
	protected int getTotalMissingCount(Map<Integer, Set<String>> minCounts) {
		int result = 0;
		Set<Integer> keySet = minCounts.keySet();
		for (Integer id : keySet) {
			Entity entity = (Entity) record.getNodeByInternalId(id);
			Set<String> set = minCounts.get(id);
			for (String childName : set) {
				int missingCount = entity.getMissingCount(childName);
				result += missingCount;
			}
		}
		return result;
	}
	
	protected Integer getAttributeValidationCount(Map<Integer, Integer> map) {
		int count = 0;
		for (Integer i : map.values()) {
			count += i;
		}
		return count;
	}

	
}
