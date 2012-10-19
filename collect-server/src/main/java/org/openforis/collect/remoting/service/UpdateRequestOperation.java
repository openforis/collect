package org.openforis.collect.remoting.service;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.FieldSymbol;

/**
 * 
 * @author S. Ricci
 * 
 */
public class UpdateRequestOperation implements Proxy {

	public enum Method {
		ADD, UPDATE, DELETE, CONFIRM_ERROR, APPROVE_MISSING, UPDATE_REMARKS, APPLY_DEFAULT_VALUE;
	}

	private Integer parentEntityId;
	private String nodeName;
	private Integer nodeId;
	private Integer fieldIndex;
	private Object value;
	private Method method;
	private String remarks;
	private FieldSymbol symbol;
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public Integer getParentEntityId() {
		return parentEntityId;
	}

	public void setParentEntityId(Integer parentEntityId) {
		this.parentEntityId = parentEntityId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public FieldSymbol getSymbol() {
		return symbol;
	}

	public void setSymbol(FieldSymbol symbol) {
		this.symbol = symbol;
	}

	public Integer getFieldIndex() {
		return fieldIndex;
	}

	public void setFieldIndex(Integer fieldIndex) {
		this.fieldIndex = fieldIndex;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}


}
