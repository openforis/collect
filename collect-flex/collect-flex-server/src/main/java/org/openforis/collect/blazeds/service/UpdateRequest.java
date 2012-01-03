package org.openforis.collect.blazeds.service;

/**
 * 
 * @author M. Togna
 * 
 */
public class UpdateRequest {

	public enum Method {
		UPDATE, ADD, DELETE;
	}

	private String nodeId;
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

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
