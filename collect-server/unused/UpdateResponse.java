package org.openforis.collect.remoting.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.proxy.NodeProxy;
import org.openforis.collect.model.proxy.ValidationResultsProxy;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 */
public class UpdateResponse implements Proxy {

	private transient MessageContextHolder messageContextHolder;
	private Integer nodeId;
	private transient Node<?> node;
	private transient Map<String, Object> relevantMap;
	private transient Map<String, Object> requiredMap;
	private transient ValidationResults attributeValidationResults;
	private transient Map<String, Object> minCountValidMap;
	private transient Map<String, Object> maxCountValidMap;
	private transient NodeProxy creatednode;
	private Integer deletedNodeId;
	private Map<Integer, Object> updatedFieldValues;
	private Integer errors;
	private Integer skipped;
	private Integer missing;
	private Integer missingErrors;
	private Integer missingWarnings;
	private Integer warnings;
	
	public UpdateResponse(MessageContextHolder messageContextHolder, Node<?> node) {
		this.messageContextHolder = messageContextHolder;
		this.nodeId = node.getInternalId();
		this.node = node;
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
			return new ValidationResultsProxy(messageContextHolder, (Attribute<?, ?>) node, attributeValidationResults);
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

	public Integer getErrors() {
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getMissing() {
		return missing;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getWarnings() {
		return warnings;
	}

	public void setWarnings(Integer warnings) {
		this.warnings = warnings;
	}

	public Integer getMissingErrors() {
		return missingErrors;
	}

	public void setMissingErrors(Integer missingErrors) {
		this.missingErrors = missingErrors;
	}

	public Integer getMissingWarnings() {
		return missingWarnings;
	}

	public void setMissingWarnings(Integer missingWarnings) {
		this.missingWarnings = missingWarnings;
	}

}