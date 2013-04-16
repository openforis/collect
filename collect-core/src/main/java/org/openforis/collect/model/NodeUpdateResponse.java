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
		
		private Map<String, Boolean> childrenRelevaceMap;
		private Map<String, Boolean> childrenRequirenessMap;
		private Map<String, ValidationResultFlag> childrenMinCountValidMap;
		private Map<String, ValidationResultFlag> childrenMaxCountValidMap;
		
		public EntityUpdateResponse(Entity node) {
			super(node);
			childrenRelevaceMap = new HashMap<String, Boolean>();
			childrenRequirenessMap = new HashMap<String, Boolean>();
			childrenMinCountValidMap = new HashMap<String, ValidationResultFlag>();
			childrenMaxCountValidMap = new HashMap<String, ValidationResultFlag>();
		}
		
		public Map<String, Boolean> getChildrenRelevance() {
			return childrenRelevaceMap;
		}

		public void setChildrenRelevance(String childName, Boolean relevant) {
			childrenRelevaceMap.put(childName, relevant);
		}

		public Map<String, Boolean> getChildrenRequireness() {
			return childrenRequirenessMap;
		}

		public void setRequired(String childName, Boolean required) {
			childrenRequirenessMap.put(childName, required);
		}

		public Map<String, ValidationResultFlag> getChildrenMinCountValidation() {
			return childrenMinCountValidMap;
		}

		public void setChildrenMinCountValid(String childName, ValidationResultFlag minCountValid) {
			childrenMinCountValidMap.put(childName, minCountValid);
		}

		public Map<String, ValidationResultFlag> getChildrenMaxCountValidation() {
			return childrenMaxCountValidMap;
		}

		public void setChildrenMaxCountValid(String childName, ValidationResultFlag maxCountValid) {
			childrenMaxCountValidMap.put(childName, maxCountValid);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime
					* result
					+ ((childrenMaxCountValidMap == null) ? 0
							: childrenMaxCountValidMap.hashCode());
			result = prime
					* result
					+ ((childrenMinCountValidMap == null) ? 0
							: childrenMinCountValidMap.hashCode());
			result = prime
					* result
					+ ((childrenRelevaceMap == null) ? 0 : childrenRelevaceMap
							.hashCode());
			result = prime
					* result
					+ ((childrenRequirenessMap == null) ? 0
							: childrenRequirenessMap.hashCode());
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
			if (childrenMaxCountValidMap == null) {
				if (other.childrenMaxCountValidMap != null)
					return false;
			} else if (!childrenMaxCountValidMap
					.equals(other.childrenMaxCountValidMap))
				return false;
			if (childrenMinCountValidMap == null) {
				if (other.childrenMinCountValidMap != null)
					return false;
			} else if (!childrenMinCountValidMap
					.equals(other.childrenMinCountValidMap))
				return false;
			if (childrenRelevaceMap == null) {
				if (other.childrenRelevaceMap != null)
					return false;
			} else if (!childrenRelevaceMap.equals(other.childrenRelevaceMap))
				return false;
			if (childrenRequirenessMap == null) {
				if (other.childrenRequirenessMap != null)
					return false;
			} else if (!childrenRequirenessMap
					.equals(other.childrenRequirenessMap))
				return false;
			return true;
		}
		
	}
	
	public static class DeleteNodeResponse extends NodeUpdateResponse<Node<?>> {
		
		public DeleteNodeResponse(Node<?> node) {
			super(node);
		}
		
	}
	
	public static interface AddNodeResponse {
		
	}

	public static class AddAttributeResponse extends AttributeUpdateResponse implements AddNodeResponse {
		
		public AddAttributeResponse(Attribute<?, ?> node) {
			super(node);
		}

	}
	
	public static class AddEntityResponse extends EntityUpdateResponse implements AddNodeResponse {
		
		public AddEntityResponse(Entity node) {
			super(node);
		}

	}
	
}