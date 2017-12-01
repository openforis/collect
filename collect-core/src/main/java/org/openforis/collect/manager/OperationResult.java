package org.openforis.collect.manager;

public class OperationResult {

	private boolean success = true;
	private String errorCode;
	private String errorMessage;

	public OperationResult() {
	}

	public OperationResult(boolean success) {
		this(success, null, null);
	}

	public OperationResult(boolean success, String errorCode, String errorMessage) {
		super();
		this.success = success;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
