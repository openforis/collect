package org.openforis.collect.remoting.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.proxy.ValidationResultsProxy;
import org.openforis.idm.metamodel.validation.ValidationResults;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 */
public class UpdateResponse implements Proxy {

	private transient int nodeId;
	private transient Map<String, Object> relevantMap;
	private transient Map<String, Object> requiredMap;
	private transient ValidationResults validationResults;
	private transient Map<String, Object> minCountValidMap;
	private transient Map<String, Object> maxCountValidMap;

	public UpdateResponse(int nodeId) {
		this.nodeId = nodeId;
		relevantMap = new HashMap<String, Object>();
		requiredMap = new HashMap<String, Object>();
		minCountValidMap = new HashMap<String, Object>();
		maxCountValidMap = new HashMap<String, Object>();
	}

	@ExternalizedProperty
	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	@ExternalizedProperty
	public Map<String, Object> getRelevant() {
//		return relevantMap;
		return null;
	}

	public void setRelevant(String childName, Object relevant) {
		relevantMap.put(childName, relevant);
	}

	@ExternalizedProperty
	public Map<String, Object> getRequired() {
//		return requiredMap;
		return null;
	}

	public void setRequired(String childName, Object required) {
		requiredMap.put(childName, required);
	}

	@ExternalizedProperty
	public ValidationResultsProxy getValidationResults() {
//		if (validationResults != null) {
//			return new ValidationResultsProxy(validationResults);
//		}
		return null;
	}

	public void setValidationResults(ValidationResults validationResults) {
		this.validationResults = validationResults;
	}

	@ExternalizedProperty
	public Map<String, Object> getMinCountValid() {
//		return minCountValidMap;
		return null;
	}

	public void setMinCountValid(String childName, Object minCountValid) {
		minCountValidMap.put(childName, minCountValid);
	}

	@ExternalizedProperty
	public Map<String, Object> getMaxCountValid() {
//		return maxCountValidMap;
		return null;
	}

	public void setMaxCountValid(String childName, Object maxCountValid) {
		maxCountValidMap.put(childName, maxCountValid);
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
