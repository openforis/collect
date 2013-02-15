package org.openforis.collect.manager.referenceDataImport;

import java.util.Arrays;

/**
 * 
 * @author S. Ricci
 *
 */
public class ParsingError {

	public enum ErrorType {
		UNEXPECTED_COLUMNS, WRONG_COLUMN_NAME, EMPTY, INVALID_VALUE, DUPLICATE_VALUE, UNEXPECTED_SYNONYM, IOERROR;
	}

	private long row;
	private String column;
	private String[] messageArgs;
	private String message;
	private ErrorType errorType;
	
	public ParsingError(ErrorType type) {
		this(type, -1, (String) null, (String) null);
	}
	
	public ParsingError(ErrorType type, String message) {
		this(type, -1, (String) null, message);
	}
	
	public ParsingError(long row, String column) {
		this(ErrorType.INVALID_VALUE, row, column, (String) null);
	}
	
	public ParsingError(ErrorType type, long row, String column, String message) {
		super();
		this.errorType = type;
		this.row = row;
		this.column = column;
		this.message = message;
	}

	public ParsingError(ErrorType type, long row, String column) {
		this(type, row, column, (String) null);
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
	
	public String[] getMessageArgs() {
		return messageArgs;
	}

	public void setMessageArgs(String[] messageArgs) {
		this.messageArgs = messageArgs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result
				+ ((errorType == null) ? 0 : errorType.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + Arrays.hashCode(messageArgs);
		result = prime * result + (int) (row ^ (row >>> 32));
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
		ParsingError other = (ParsingError) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (errorType != other.errorType)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (!Arrays.equals(messageArgs, other.messageArgs))
			return false;
		if (row != other.row)
			return false;
		return true;
	}
	
}
