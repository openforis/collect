package org.openforis.collect.io.metadata.collectearth;

import java.util.List;

public class CSVFileValidationResult {
	
	public enum ErrorType {
		INVALID_HEADERS, INVALID_FILE_TYPE, INVALID_CONTENT_IN_LINE, INVALID_NUMBER_OF_COLUMNS
	}
	
	private boolean successful;
	private ErrorType errorType;
	private List<String> expectedHeaders;
	private List<String> foundHeaders;
	private Integer rowNumber;
	private String message;
	private List<CSVRowValidationResult> rowValidations;
	
	
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

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setRowValidations(List<CSVRowValidationResult> rowValidations) {
		this.rowValidations = rowValidations;
	}

	public List<CSVRowValidationResult> getRowValidations() {
		return rowValidations;
	}
	
	
	
	
}