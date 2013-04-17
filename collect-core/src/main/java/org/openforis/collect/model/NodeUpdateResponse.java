package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class NodeUpdateResponse<T extends Node<?>> {

	protected T node;
	
	public NodeUpdateResponse(T node) {
		this.node = node;
	}

	public T getNode() {
		return node;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeUpdateResponse<?> other = (NodeUpdateResponse<?>) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}
	
	public static class AttributeUpdateResponse extends NodeUpdateResponse<Attribute<?, ?>> {
		
		private ValidationResults validationResults;
		private Map<Integer, Object> updatedFieldValues;
		
		public AttributeUpdateResponse(Attribute<?, ?> node) {
			super(node);
		}

		public Map<Integer, Object> getUpdatedFieldValues() {
			return updatedFieldValues;
		}
	
		public void setUpdatedFieldValues(Map<Integer, Object> updatedFieldValues) {
			this.updatedFieldValues = updatedFieldValues;
		}

		public ValidationResults getValidationResults() {
			return validationResults;
		}

		public void setValidationResults(
				ValidationResults validationResults) {
			this.validationResults = validationResults;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime
					* result
					+ ((updatedFieldValues == null) ? 0 : updatedFieldValues
							.hashCode());
			result = prime
					* result
					+ ((validationResults == null) ? 0 : validationResults
							.hashCode());
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
			AttributeUpdateResponse other = (AttributeUpdateResponse) obj;
			if (updatedFieldValues == null) {
				if (other.updatedFieldValues != null)
					return false;
			} else if (!updatedFieldValues.equals(other.updatedFieldValues))
				return false;
			if (validationResults == null) {
				if (other.validationResults != null)
					return false;
			} else if (!validationResults.equals(other.validationResults))
				return false;
			return true;
		}
		
	}
	
	public static class EntityUpdateResponse extends NodeUpdateResponse<Entity> {
		
		private Map<String, Boolean> childrenRelevance;
		private Map<String, Boolean> childrenRequireness;
		private Map<String, ValidationResultFlag> childrenMinCountValidation;
		private Map<String, ValidationResultFlag> childrenMaxCountValidation;
		
		public EntityUpdateResponse(Entity node) {
			super(node);
			childrenRelevance = new HashMap<String, Boolean>();
			childrenRequireness = new HashMap<String, Boolean>();
			childrenMinCountValidation = new HashMap<String, ValidationResultFlag>();
			childrenMaxCountValidation = new HashMap<String, ValidationResultFlag>();
		}
		
		public Map<String, Boolean> getChildrenRelevance() {
			return childrenRelevance;
		}

		public void setChildrenRelevance(String childName, Boolean relevant) {
			childrenRelevance.put(childName, relevant);
		}
		
		public void setChildrenRelevance(Map<String, Boolean> map) {
			childrenRelevance = map;
		}

		public Map<String, Boolean> getChildrenRequireness() {
			return childrenRequireness;
		}

		public void setChildrenRequireness(String childName, Boolean required) {
			childrenRequireness.put(childName, required);
		}
		
		public void setChildrenRequireness(Map<String, Boolean> map) {
			childrenRequireness = map;
		}

		public Map<String, ValidationResultFlag> getChildrenMinCountValidation() {
			return childrenMinCountValidation;
		}

		public void setChildrenMinCountValidation(String childName, ValidationResultFlag minCountValid) {
			childrenMinCountValidation.put(childName, minCountValid);
		}

		public void setChildrenMinCountValidation(Map<String, ValidationResultFlag> map) {
			childrenMinCountValidation = map;
		}
	
		public Map<String, ValidationResultFlag> getChildrenMaxCountValidation() {
			return childrenMaxCountValidation;
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
			EntityUpdateResponse other = (EntityUpdateResponse) obj;
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
	
	public static class NodeDeleteResponse extends NodeUpdateResponse<Node<?>> {
		
		public NodeDeleteResponse(Node<?> node) {
			super(node);
		}
		
	}
	
	public static interface NodeAddResponse {
		
	}

	public static class AttributeAddResponse extends AttributeUpdateResponse implements NodeAddResponse {
		
		public AttributeAddResponse(Attribute<?, ?> node) {
			super(node);
		}

		public void merge(AttributeUpdateResponse newResponse) {
			this.setUpdatedFieldValues(newResponse.getUpdatedFieldValues());
			this.setValidationResults(newResponse.getValidationResults());
		}
		
	}
	
	public static class EntityAddResponse extends EntityUpdateResponse implements NodeAddResponse {
		
		public EntityAddResponse(Entity node) {
			super(node);
		}
		
		public void merge(EntityUpdateResponse newResponse) {
			this.setChildrenMinCountValidation(newResponse.getChildrenMinCountValidation());
			this.setChildrenMaxCountValidation(newResponse.getChildrenMaxCountValidation());
			this.setChildrenRelevance(newResponse.getChildrenRelevance());
			this.setChildrenRequireness(newResponse.getChildrenRequireness());
		}

	}
	
}