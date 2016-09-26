package org.openforis.collect.io.metadata.collectearth;

import java.util.List;

public class CSVFileValidationResult {
	
	public enum ErrorType {
		INVALID_HEADERS, INVALID_FILE_TYPE
	}
	
	private boolean successful;
	private ErrorType errorType;
	private List<String> expectedHeaders;
	private List<String> foundHeaders;
	
	public CSVFileValidationResult() {
		this.successful = true;
	}

	public CSVFileValidationResult(ErrorType errorType) {
		this(errorType, null, null);
	}

	public CSVFileValidationResult(ErrorType errorType, List<String> expectedHeaders, List<String> foundHeaders) {
		super();
		this.successful = false;
		this.errorType = errorType;
		this.expectedHeaders = expectedHeaders;
		this.foundHeaders = foundHeaders;
	}
	
	public boolean isSuccessful() {
		return successful;
	}
	
	public ErrorType getErrorType() {
		return errorType;
	}
	
	public List<String> getExpectedHeaders() {
		return expectedHeaders;
	}
	
	public List<String> getFoundHeaders() {
		return foundHeaders;
	}
	
}