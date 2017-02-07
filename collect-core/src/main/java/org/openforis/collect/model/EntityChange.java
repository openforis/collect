package org.openforis.collect.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * Change related to an Entity of a Record.
 * It includes relevance, requireness and cardinality validations 
 * for each children of the entity.
 * 
 * @author S. Ricci
 *
 */
public class EntityChange extends NodeChange<Entity> {
	
	protected Map<String, Boolean> relevanceByChildName;
	protected Map<Integer, Integer> minCountByChildDefinitionId;
	protected Map<Integer, Integer> maxCountByChildDefinitionId;
	protected Map<String, ValidationResultFlag> minCountValidationByChildName;
	protected Map<String, ValidationResultFlag> maxCountValidationByChildName;
	
	public EntityChange(Entity node) {
		super(node.getRecord().getId(), node.getAncestorIds(), node);
		List<NodeDefinition> childDefs = node.getDefinition().getChildDefinitions();
		int numChildren = childDefs.size();
		minCountByChildDefinitionId = new HashMap<Integer, Integer>(numChildren);
		maxCountByChildDefinitionId = new HashMap<Integer, Integer>(numChildren);
		minCountValidationByChildName = new HashMap<String, ValidationResultFlag>(numChildren);
		maxCountValidationByChildName = new HashMap<String, ValidationResultFlag>(numChildren);
		relevanceByChildName = new HashMap<String, Boolean>(numChildren);
	}
	
	public void merge(EntityChange newChange) {
		minCountByChildDefinitionId.putAll(newChange.getMinCountByChildDefinitionId());
		maxCountByChildDefinitionId.putAll(newChange.getMaxCountByChildDefinitionId());
		minCountValidationByChildName.putAll(newChange.getChildrenMinCountValidation());
		maxCountValidationByChildName.putAll(newChange.getChildrenMaxCountValidation());
		relevanceByChildName.putAll(newChange.getChildrenRelevance());
	}
	
	public Map<String, Boolean> getChildrenRelevance() {
		return CollectionUtils.unmodifiableMap(relevanceByChildName);
	}

	public void setRelevance(String childName, Boolean relevant) {
		relevanceByChildName.put(childName, relevant);
	}
	
	public void setChildrenRelevance(Map<String, Boolean> map) {
		relevanceByChildName = map;
	}

	public Map<Integer, Integer> getMinCountByChildDefinitionId() {
		return CollectionUtils.unmodifiableMap(minCountByChildDefinitionId);
	}

	public void setMinCount(Integer childDefinitionId, int count) {
		minCountByChildDefinitionId.put(childDefinitionId, count);
	}
	
	public Map<Integer, Integer> getMaxCountByChildDefinitionId() {
		return CollectionUtils.unmodifiableMap(maxCountByChildDefinitionId);
	}

	public void setMaxCount(Integer childDefinitionId, int count) {
		maxCountByChildDefinitionId.put(childDefinitionId, count);
	}
	
	public Map<String, ValidationResultFlag> getChildrenMinCountValidation() {
		return CollectionUtils.unmodifiableMap(minCountValidationByChildName);
	}

	public void setMinCountValidation(String childName, ValidationResultFlag minCountValid) {
		minCountValidationByChildName.put(childName, minCountValid);
	}

	public Map<String, ValidationResultFlag> getChildrenMaxCountValidation() {
		return CollectionUtils.unmodifiableMap(maxCountValidationByChildName);
	}

	public void setMaxCountValidation(String childName, ValidationResultFlag maxCountValid) {
		maxCountValidationByChildName.put(childName, maxCountValid);
	}
	
	public Set<Node<?>> extractChangedNodes() {
		Set<Node<?>> result = new HashSet<Node<?>>();
		result.addAll(extractNodes(getChildrenMaxCountValidation().keySet()));
		result.addAll(extractNodes(getChildrenMinCountValidation().keySet()));
		result.addAll(extractNodes(getChildrenRelevance().keySet()));
		return result;
	}
	
	private Set<Node<?>> extractNodes(Set<String> childNames) {
		Set<Node<?>> result = new HashSet<Node<?>>();
		for (String childName : childNames) {
			NodeDefinition childDef = getNode().getDefinition().getChildDefinition(childName);
			if (childDef instanceof AttributeDefinition) {
				result.addAll(node.getChildren(childDef));
			}
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((maxCountValidationByChildName == null) ? 0
						: maxCountValidationByChildName.hashCode());
		result = prime
				* result
				+ ((minCountValidationByChildName == null) ? 0
						: minCountValidationByChildName.hashCode());
		result = prime
				* result
				+ ((relevanceByChildName == null) ? 0 : relevanceByChildName
						.hashCode());
		result = prime
				* result
				+ ((minCountByChildDefinitionId == null) ? 0
						: minCountByChildDefinitionId.hashCode());
		result = prime
				* result
				+ ((maxCountByChildDefinitionId == null) ? 0
						: maxCountByChildDefinitionId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityChange other = (EntityChange) obj;
		if (maxCountValidationByChildName == null) {
			if (other.maxCountValidationByChildName != null)
				return false;
		} else if (!maxCountValidationByChildName
				.equals(other.maxCountValidationByChildName))
			return false;
		if (minCountValidationByChildName == null) {
			if (other.minCountValidationByChildName != null)
				return false;
		} else if (!minCountValidationByChildName
				.equals(other.minCountValidationByChildName))
			return false;
		if (relevanceByChildName == null) {
			if (other.relevanceByChildName != null)
				return false;
		} else if (!relevanceByChildName.equals(other.relevanceByChildName))
			return false;
		if (minCountByChildDefinitionId == null) {
			if (other.minCountByChildDefinitionId != null)
				return false;
		} else if (!minCountByChildDefinitionId
				.equals(other.minCountByChildDefinitionId))
			return false;
		if (maxCountByChildDefinitionId == null) {
			if (other.maxCountByChildDefinitionId != null)
				return false;
		} else if (!maxCountByChildDefinitionId
				.equals(other.maxCountByChildDefinitionId))
			return false;
		return true;
	}
	
}