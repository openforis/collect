package org.openforis.collect.io.data;

import org.openforis.collect.manager.RecordManager.RecordOperations;

public class MissingStepsException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private RecordOperations operations;

	public MissingStepsException(RecordOperations operations) {
		super("Missing steps in record operations " + operations);
		this.operations = operations;
	}

	public RecordOperations getOperations() {
		return operations;
	}
	
}
