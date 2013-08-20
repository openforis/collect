package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.Entity;

/**
 * Change related to an Entity of a Record.
 * It includes relevance, requireness and cardinality validations 
 * for each children of the entity.
 * 
 * @author S. Ricci
 *
 */
public class EntityChange extends NodeChange<Entity> {
	
	protected Map<String, Boolean> childrenRelevance;
	protected Map<String, Boolean> childrenRequireness;
	protected Map<String, ValidationResultFlag> childrenMinCountValidation;
	protected Map<String, ValidationResultFlag> childrenMaxCountValidation;
	
	public EntityChange(Entity node) {
		super(node);
		childrenRelevance = new HashMap<String, Boolean>();
		childrenRequireness = new HashMap<String, Boolean>();
		childrenMinCountValidation = new HashMap<String, ValidationResultFlag>();
		childrenMaxCountValidation = new HashMap<String, ValidationResultFlag>();
	}
	
	public void merge(EntityChange newChange) {
		this.childrenMinCountValidation.putAll(newChange.getChildrenMinCountValidation());
		this.childrenMaxCountValidation.putAll(newChange.getChildrenMaxCountValidation());
		this.childrenRelevance.putAll(newChange.getChildrenRelevance());
		this.childrenRequireness.putAll(newChange.getChildrenRequireness());
	}
	
	public Map<String, Boolean> getChildrenRelevance() {
		return CollectionUtils.unmodifiableMap(childrenRelevance);
	}

	public void setChildrenRelevance(String childName, Boolean relevant) {
		childrenRelevance.put(childName, relevant);
	}
	
	public void setChildrenRelevance(Map<String, Boolean> map) {
		childrenRelevance = map;
	}

	public Map<String, Boolean> getChildrenRequireness() {
		return CollectionUtils.unmodifiableMap(childrenRequireness);
	}

	public void setChildrenRequireness(String childName, Boolean required) {
		childrenRequireness.put(childName, required);
	}
	
	public void setChildrenRequireness(Map<String, Boolean> map) {
		childrenRequireness = map;
	}

	public Map<String, ValidationResultFlag> getChildrenMinCountValidation() {
		return CollectionUtils.unmodifiableMap(childrenMinCountValidation);
	}

	public void setChildrenMinCountValidation(String childName, ValidationResultFlag minCountValid) {
		childrenMinCountValidation.put(childName, minCountValid);
	}

	public void setChildrenMinCountValidation(Map<String, ValidationResultFlag> map) {
		childrenMinCountValidation = map;
	}

	public Map<String, ValidationResultFlag> getChildrenMaxCountValidation() {
		return CollectionUtils.unmodifiableMap(childrenMaxCountValidation);
	}

	public void setChildrenMaxCountValidation(String childName, ValidationResultFlag maxCountValid) {
		childrenMaxCountValidation.put(childName, maxCountValid);
	}

	public void setChildrenMaxCountValidation(Map<String, ValidationResultFlag> map) {
		childrenMaxCountValidation = map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((childrenMaxCountValidation == null) ? 0
						: childrenMaxCountValidation.hashCode());
		result = prime
				* result
				+ ((childrenMinCountValidation == null) ? 0
						: childrenMinCountValidation.hashCode());
		result = prime
				* result
				+ ((childrenRelevance == null) ? 0 : childrenRelevance
						.hashCode());
		result = prime
				* result
				+ ((childrenRequireness == null) ? 0
						: childrenRequireness.hashCode());
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
		if (childrenMaxCountValidation == null) {
			if (other.childrenMaxCountValidation != null)
				return false;
		} else if (!childrenMaxCountValidation
				.equals(other.childrenMaxCountValidation))
			return false;
		if (childrenMinCountValidation == null) {
			if (other.childrenMinCountValidation != null)
				return false;
		} else if (!childrenMinCountValidation
				.equals(other.childrenMinCountValidation))
			return false;
		if (childrenRelevance == null) {
			if (other.childrenRelevance != null)
				return false;
		} else if (!childrenRelevance.equals(other.childrenRelevance))
			return false;
		if (childrenRequireness == null) {
			if (other.childrenRequireness != null)
				return false;
		} else if (!childrenRequireness
				.equals(other.childrenRequireness))
			return false;
		return true;
	}
	
}