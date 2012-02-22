/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.metamodel.validation.ValidationResults;

/**
 * @author M. Togna
 * 
 */
public class NodeState {

	private int nodeId;
	private boolean relevant;
	private boolean required;
	private ValidationResults validationResults;
	private Object defaultValue;

	public NodeState(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getNodeId() {
		return nodeId;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public ValidationResults getValidationResults() {
		return validationResults;
	}

	public void setValidationResults(ValidationResults validationResults) {
		this.validationResults = validationResults;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
}
