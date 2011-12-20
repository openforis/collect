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

	private String modelObjectId;
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

	public String getModelObjectId() {
		return modelObjectId;
	}

	public void setModelObjectId(String modelObjectId) {
		this.modelObjectId = modelObjectId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
