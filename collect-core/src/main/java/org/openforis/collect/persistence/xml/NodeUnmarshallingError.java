package org.openforis.collect.persistence.xml;

import org.openforis.collect.model.CollectRecord.Step;

public class NodeUnmarshallingError {
	
	private Step step;
	private String path;
	private String message;
	
	public NodeUnmarshallingError(String message) {
		this.message = message;
	}
	
	public NodeUnmarshallingError(Step step, String path, String message) {
		this(message);
		this.step = step;
		this.path = path;
	}
	
	public Step getStep() {
		return step;
	}
	
	public void setStep(Step step) {
		this.step = step;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "step: " + step + " - node: " + path + " - message: " + message;
	}

}