package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.Proxy;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class UpdateRequest implements Proxy {

	private boolean autoSave;
	private List<UpdateRequestOperation> operations;
	
	public UpdateRequest() {
		autoSave = false;
	}

	public List<UpdateRequestOperation> getOperations() {
		return operations;
	}

	public void setOperations(List<UpdateRequestOperation> operations) {
		this.operations = operations;
	}

	public boolean isAutoSave() {
		return autoSave;
	}

	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}
}
