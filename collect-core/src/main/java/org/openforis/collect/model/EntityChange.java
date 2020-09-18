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
	
	protected Map<Integer, Boolean> relevanceByChildDefinitionId;
	protected Map<Integer, Integer> minCountByChildDefinitionId;
	protected Map<Integer, Integer> maxCountByChildDefinitionId;
	protected Map<Integer, ValidationResultFlag> minCountValidationByChildDefinitionId;
	protected Map<Integer, ValidationResultFlag> maxCountValidationByChildDefinitionId;
	
	public EntityChange(Entity node) {
		super(node.getRecord().getId(), node.getAncestorIds(), node);
		List<NodeDefinition> childDefs = node.getDefinition().getChildDefinitions();
		int numChildren = childDefs.size();
		minCountByChildDefinitionId = new HashMap<Integer, Integer>(numChildren);
		maxCountByChildDefinitionId = new HashMap<Integer, Integer>(numChildren);
		minCountValidationByChildDefinitionId = new HashMap<Integer, ValidationResultFlag>(numChildren);
		maxCountValidationByChildDefinitionId = new HashMap<Integer, ValidationResultFlag>(numChildren);
		relevanceByChildDefinitionId = new HashMap<Integer, Boolean>(numChildren);
	}
	
	public void merge(EntityChange newChange) {
		minCountByChildDefinitionId.putAll(newChange.getMinCountByChildDefinitionId());
		maxCountByChildDefinitionId.putAll(newChange.getMaxCountByChildDefinitionId());
		minCountValidationByChildDefinitionId.putAll(newChange.getChildrenMinCountValidation());
		maxCountValidationByChildDefinitionId.putAll(newChange.getChildrenMaxCountValidation());
		relevanceByChildDefinitionId.putAll(newChange.getChildrenRelevance());
	}
	
	public Map<Integer, Boolean> getChildrenRelevance() {
		return CollectionUtils.unmodifiableMap(relevanceByChildDefinitionId);
	}

	public void setRelevance(int childDefinitionId, Boolean relevant) {
		relevanceByChildDefinitionId.put(childDefinitionId, relevant);
	}
	
	public void setChildrenRelevance(Map<Integer, Boolean> map) {
		relevanceByChildDefinitionId = map;
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
	
	public Map<Integer, ValidationResultFlag> getChildrenMinCountValidation() {
		return CollectionUtils.unmodifiableMap(minCountValidationByChildDefinitionId);
	}

	public void setMinCountValidation(int childDefinitionId, ValidationResultFlag minCountValid) {
		minCountValidationByChildDefinitionId.put(childDefinitionId, minCountValid);
	}

	public Map<Integer, ValidationResultFlag> getChildrenMaxCountValidation() {
		return CollectionUtils.unmodifiableMap(maxCountValidationByChildDefinitionId);
	}

	public void setMaxCountValidation(int childDefinitionId, ValidationResultFlag maxCountValid) {
		maxCountValidationByChildDefinitionId.put(childDefinitionId, maxCountValid);
	}
	
	public Set<Node<?>> extractChangedNodes() {
		Set<Node<?>> result = new HashSet<Node<?>>();
		result.addAll(extractNodes(getChildrenMaxCountValidation().keySet()));
		result.addAll(extractNodes(getChildrenMinCountValidation().keySet()));
		result.addAll(extractNodes(getChildrenRelevance().keySet()));
		return result;
	}
	
	private Set<Node<?>> extractNodes(Set<Integer> childDefinitionIds) {
		Set<Node<?>> result = new HashSet<Node<?>>();
		for (Integer childDefinitionId : childDefinitionIds) {
			NodeDefinition childDef = getNode().getDefinition().getChildDefinition(childDefinitionId);
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
				+ ((maxCountValidationByChildDefinitionId == null) ? 0
						: maxCountValidationByChildDefinitionId.hashCode());
		result = prime
				* result
				+ ((minCountValidationByChildDefinitionId == null) ? 0
						: minCountValidationByChildDefinitionId.hashCode());
		result = prime
				* result
				+ ((relevanceByChildDefinitionId == null) ? 0 : relevanceByChildDefinitionId
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
		if (maxCountValidationByChildDefinitionId == null) {
			if (other.maxCountValidationByChildDefinitionId != null)
				return false;
		} else if (!maxCountValidationByChildDefinitionId
				.equals(other.maxCountValidationByChildDefinitionId))
			return false;
		if (minCountValidationByChildDefinitionId == null) {
			if (other.minCountValidationByChildDefinitionId != null)
				return false;
		} else if (!minCountValidationByChildDefinitionId
				.equals(other.minCountValidationByChildDefinitionId))
			return false;
		if (relevanceByChildDefinitionId == null) {
			if (other.relevanceByChildDefinitionId != null)
				return false;
		} else if (!relevanceByChildDefinitionId.equals(other.relevanceByChildDefinitionId))
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