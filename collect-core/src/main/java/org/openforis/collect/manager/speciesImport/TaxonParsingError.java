package org.openforis.collect.manager.speciesImport;

import org.openforis.collect.manager.speciesImport.TaxonCSVReader.Column;


/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonParsingError {
	
	private long row;
	private String column;
	private String message;
	private ErrorType errorType;
	
	public enum ErrorType {
		WRONG_HEADER, EMPTY, INVALID_VALUE, DUPLICATE_VALUE, IOERROR;
	}
	
	public TaxonParsingError(ErrorType type) {
		this(type, -1, (String) null, (String) null);
	}
	
	public TaxonParsingError(ErrorType type, String message) {
		this(type, -1, (String) null, message);
	}
	
	public TaxonParsingError(long row, String column) {
		this(ErrorType.INVALID_VALUE, row, column, (String) null);
	}
	
	public TaxonParsingError(ErrorType type, long row, String column, String message) {
		super();
		this.errorType = type;
		this.row = row;
		this.column = column;
		this.message = message;
	}

	public TaxonParsingError(ErrorType type, long row, Column column) {
		this(type, row, column, (String) null);
	}	
	
	public TaxonParsingError(long row, Column column) {
		this(ErrorType.INVALID_VALUE, row, column, (String) null);
	}	
	
	public TaxonParsingError(ErrorType type, long row, Column column, String message) {
		this(type, row, column.getName(), message);
	}
	
	public TaxonParsingError(long row, Column column, String message) {
		this(ErrorType.INVALID_VALUE, row, column.getName(), message);
	}	
	
	public ErrorType getErrorType() {
		return errorType;
	}
	
	public long getRow() {
		return row;
	}
	
	public String getColumn() {
		return column;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (int) (row ^ (row >>> 32));
		result = prime * result + ((errorType == null) ? 0 : errorType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaxonParsingError other = (TaxonParsingError) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (row != other.row)
			return false;
		if (errorType != other.errorType)
			return false;
		return true;
	}
	
}