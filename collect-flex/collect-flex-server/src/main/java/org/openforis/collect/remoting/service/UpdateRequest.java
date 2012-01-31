package org.openforis.collect.remoting.service;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class UpdateRequest {

	public enum Method {
		UPDATE, ADD, DELETE;
	}

	private Integer parentNodeId;
	private String attributeName;
	private Integer nodeId;
	private String value;
	private Method method;
	
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

	public Integer getParentNodeId() {
		return parentNodeId;
	}

	public void setParentNodeId(Integer parentNodeId) {
		this.parentNodeId = parentNodeId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

}
