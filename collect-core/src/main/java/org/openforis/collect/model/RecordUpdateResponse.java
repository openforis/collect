package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;

import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Node;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 */
public class RecordUpdateResponse {

	private Node<?> node;
	private Map<String, Object> relevantMap;
	private Map<String, Object> requiredMap;
	private ValidationResults attributeValidationResults;
	private Map<String, Object> minCountValidMap;
	private Map<String, Object> maxCountValidMap;
	private Node<?> creatednode;
	private Integer deletedNodeId;
	private Map<Integer, Object> updatedFieldValues;
	
	public RecordUpdateResponse(Node<?> node) {
		this.node = node;
		relevantMap = new HashMap<String, Object>();
		requiredMap = new HashMap<String, Object>();
		minCountValidMap = new HashMap<String, Object>();
		maxCountValidMap = new HashMap<String, Object>();
	}

	public Node<?> getNode() {
		return node;
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

	public ValidationResults getValidationResults() {
		return attributeValidationResults;
	}

	public void setAttributeValidationResults(ValidationResults validationResults) {
		this.attributeValidationResults = validationResults;
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

	public Node<?> getCreatedNode() {
		return this.creatednode;
	}
	
	public void setCreatedNode(Node<?> node) {
		this.creatednode = node;
	}
	
	public Integer getDeletedNodeId() {
		return deletedNodeId;
	}

	public void setDeletedNodeId(Integer deletedNodeId) {
		this.deletedNodeId = deletedNodeId;
	}

	public Map<Integer, Object> getUpdatedFieldValues() {
		return updatedFieldValues;
	}

	public void setUpdatedFieldValues(Map<Integer, Object> updatedFieldValues) {
		this.updatedFieldValues = updatedFieldValues;
	}


	

}