package org.openforis.collect.io.metadata.collectearth;

import java.util.List;

import org.openforis.collect.io.metadata.collectearth.CSVFileValidationResult.ErrorType;

public class CSVRowValidationResult {

	Integer rowNumber;
	String message;
	List<String> expectedRows;
	ErrorType errorType;
		
	public CSVRowValidationResult(Integer rowNumber, ErrorType errorType) {
		super();
		this.rowNumber = rowNumber;
		this.errorType = errorType;
	}
	
	public CSVRowValidationResult(Integer rowNumber, String message) {
		super();
		this.rowNumber = rowNumber;
		this.message = message;
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

	public List<String> getExpectedRows() {
		return expectedRows;
	}

	public void setExpectedColumns(List<String> expectedRows) {
		this.expectedRows = expectedRows;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}
}
