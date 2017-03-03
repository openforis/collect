package org.openforis.collect.io.metadata.collectearth;

import java.util.List;

public class CSVFileValidationResult {
	
	public enum ErrorType {
		INVALID_HEADERS, INVALID_FILE_TYPE, INVALID_NUMBER_OF_COLUMNS, INVALID_NUMBER_OF_PLOTS_TOO_LARGE, INVALID_NUMBER_OF_PLOTS_WARNING, INVALID_VALUES_IN_CSV
	}
	
	private boolean successful;
	private ErrorType errorType;
	private List<String> expectedHeaders;
	private List<String> foundHeaders;
	private Integer numberOfRows;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setRowValidations(List<CSVRowValidationResult> rowValidations) {
		if( rowValidations != null && rowValidations.size() > 0 ){
			successful = false;
			errorType = ErrorType.INVALID_VALUES_IN_CSV;
		}
		this.rowValidations = rowValidations;
	}

	public List<CSVRowValidationResult> getRowValidations() {
		return rowValidations;
	}

	public Integer getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(Integer numberOfRows) {
		this.numberOfRows = numberOfRows;
	}
	
	
	
	
}