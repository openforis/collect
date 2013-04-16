package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;

import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class RecordUpdateResponse {

	public static class NodeUpdateResponse<T extends Node<?>> extends RecordUpdateResponse {
		
		protected T node;
		
		public NodeUpdateResponse(T node) {
			this.node = node;
		}
	
		public T getNode() {
			return node;
		}
		
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
	}
	
	public static class EntityUpdateResponse extends NodeUpdateResponse<Entity> {
		
		private Map<String, Object> relevantMap;
		private Map<String, Object> requiredMap;
		private Map<String, Object> minCountValidMap;
		private Map<String, Object> maxCountValidMap;
		
		public EntityUpdateResponse(Entity node) {
			super(node);
			relevantMap = new HashMap<String, Object>();
			requiredMap = new HashMap<String, Object>();
			minCountValidMap = new HashMap<String, Object>();
			maxCountValidMap = new HashMap<String, Object>();
		}
		
		public Map<String, Object> getRelevant() {
			return relevantMap;
		}

		public void setRelevant(String childName, Object relevant) {
			relevantMap.put(childName, relevant);
		}

		public Map<String, Object> getRequired() {
			return requiredMap;
		}

		public void setRequired(String childName, Object required) {
			requiredMap.put(childName, required);
		}

		public Map<String, Object> getMinCountValidation() {
			return minCountValidMap;
		}

		public void setMinCountValid(String childName, Object minCountValid) {
			minCountValidMap.put(childName, minCountValid);
		}

		public Map<String, Object> getMaxCountValidation() {
			return maxCountValidMap;
		}

		public void setMaxCountValid(String childName, Object maxCountValid) {
			maxCountValidMap.put(childName, maxCountValid);
		}
	}
	
	public static class DeleteNodeResponse extends RecordUpdateResponse {
		
		private Integer deletedNodeId;
		
		public Integer getDeletedNodeId() {
			return deletedNodeId;
		}
	
		public void setDeletedNodeId(Integer deletedNodeId) {
			this.deletedNodeId = deletedNodeId;
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