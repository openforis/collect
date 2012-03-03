package org.openforis.collect.remoting.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.proxy.NodeProxy;
import org.openforis.collect.model.proxy.ValidationResultsProxy;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Node;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 */
public class UpdateResponse implements Proxy {

	private transient Integer internalNodeId;
	private transient Map<String, Object> relevantMap;
	private transient Map<String, Object> requiredMap;
	private transient ValidationResults attributeValidationResults;
	private transient Map<String, Object> minCountValidMap;
	private transient Map<String, Object> maxCountValidMap;
	private transient Node<?> creatednode;
	private transient Integer deletedINodeInternalId;
	
	public UpdateResponse(int nodeId) {
		this.internalNodeId = nodeId;
		relevantMap = new HashMap<String, Object>();
		requiredMap = new HashMap<String, Object>();
		minCountValidMap = new HashMap<String, Object>();
		maxCountValidMap = new HashMap<String, Object>();
	}

	@ExternalizedProperty
	public Integer getNodeId() {
		return internalNodeId;
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
		if(creatednode != null){
			return new NodeProxy(creatednode);
		}
		return null;
	}
	
	@ExternalizedProperty
	public Integer getDeletedINodeId() {
		return deletedINodeInternalId;
	}
	
	public void setDeletedINodeInternalId(Integer deletedINodeInternalId) {
		this.deletedINodeInternalId = deletedINodeInternalId;
	}
	
	public void setCreatedNode(Node<?> creatednode) {
		this.creatednode = creatednode;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(internalNodeId).toHashCode();
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
		return new EqualsBuilder().append(internalNodeId, other.internalNodeId).isEquals();
	}

}
