package org.openforis.collect.io.metadata.collectearth;

import java.util.List;

import org.openforis.collect.io.metadata.collectearth.CSVFileValidationResult.ErrorType;

public class CSVRowValidationResult {

	Integer rowNumber;
	String message;
	List<String> expectedColumns;
	ErrorType errorType;
	Integer columnPosition;
	List<String> validationMessages;
		
	public CSVRowValidationResult(Integer rowNumber, ErrorType errorType) {
		super();
		this.rowNumber = rowNumber;
		this.errorType = errorType;
	}
	
	public CSVRowValidationResult(int rowNumber, ErrorType errorType, int columnPosition, String message) {
		super();
		this.rowNumber = rowNumber;
		this.errorType = errorType;
		this.columnPosition = columnPosition;
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

	public List<String> getExpectedColumns() {
		return expectedColumns;
	}

	public void setExpectedColumns(List<String> expectedColumn) {
		this.expectedColumns = expectedColumn;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

	public Integer getColumnPosition() {
		return columnPosition;
	}

	public void setColumnPosition(Integer columnPosition) {
		this.columnPosition = columnPosition;
	}

}
