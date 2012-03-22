package org.openforis.collect.remoting.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.proxy.AttributeProxy;
import org.openforis.collect.model.proxy.EntityProxy;
import org.openforis.collect.model.proxy.NodeProxy;
import org.openforis.collect.model.proxy.ValidationResultsProxy;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 */
public class UpdateResponse implements Proxy {

	private Integer nodeId;
	private transient Map<String, Object> relevantMap;
	private transient Map<String, Object> requiredMap;
	private transient ValidationResults attributeValidationResults;
	private transient Map<String, Object> minCountValidMap;
	private transient Map<String, Object> maxCountValidMap;
	private transient NodeProxy creatednode;
	private Integer deletedNodeId;
	private Map<Integer, Object> updatedFieldValues;
	
	public UpdateResponse(int nodeId) {
		this.nodeId = nodeId;
		relevantMap = new HashMap<String, Object>();
		requiredMap = new HashMap<String, Object>();
		minCountValidMap = new HashMap<String, Object>();
		maxCountValidMap = new HashMap<String, Object>();
	}

	public Integer getNodeId() {
		return nodeId;
	}

	@ExternalizedProperty
	public Map<String, Object> getRelevant() {
		return relevantMap;
	}

	public void setRelevant(String childName, Object relevant) {
		relevantMap.put(childName, relevant);
	}

	@ExternalizedProperty
	public Map<String, Object> getRequired() {
		return requiredMap;
	}

	public void setRequired(String childName, Object required) {
		requiredMap.put(childName, required);
	}

	@ExternalizedProperty
	public ValidationResultsProxy getValidationResults() {
		if (attributeValidationResults != null) {
			return new ValidationResultsProxy(attributeValidationResults);
		}
		return null;
	}

	public void setAttributeValidationResults(ValidationResults validationResults) {
		this.attributeValidationResults = validationResults;
	}

	@ExternalizedProperty
	public Map<String, Object> getMinCountValidation() {
		return minCountValidMap;
	}

	public void setMinCountValid(String childName, Object minCountValid) {
		minCountValidMap.put(childName, minCountValid);
	}

	@ExternalizedProperty
	public Map<String, Object> getMaxCountValidation() {
		return maxCountValidMap;
	}

	public void setMaxCountValid(String childName, Object maxCountValid) {
		maxCountValidMap.put(childName, maxCountValid);
	}

	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return this.creatednode;
//		if(creatednode != null){
//			if(creatednode instanceof Attribute<?, ?>) {
//				return new AttributeProxy(null, (Attribute<?, ?>) creatednode);
//			} else if(creatednode instanceof Entity) {
//				return new EntityProxy(null, (Entity) creatednode);
//			}
//		}
//		return null;
	}
	
	public void setCreatedNode(NodeProxy nodeProxy) {
		this.creatednode = nodeProxy;
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

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(nodeId).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpdateResponse other = (UpdateResponse) obj;
		return new EqualsBuilder().append(nodeId, other.nodeId).isEquals();
	}

}