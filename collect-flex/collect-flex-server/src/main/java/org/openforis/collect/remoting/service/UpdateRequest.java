package org.openforis.collect.remoting.service;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.proxy.AttributeSymbol;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class UpdateRequest implements Proxy {

	public enum Method {
		UPDATE, ADD, DELETE;
	}

	private Integer parentEntityId;
	private String nodeName;
	private Integer nodeId;
	private String value;
	private Method method;
	private String remarks;
	private AttributeSymbol symbol;
	
	public UpdateRequest(){
		
	}
	
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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

	public AttributeSymbol getSymbol() {
		return symbol;
	}

	public void setSymbol(AttributeSymbol symbol) {
		this.symbol = symbol;
	}

}
