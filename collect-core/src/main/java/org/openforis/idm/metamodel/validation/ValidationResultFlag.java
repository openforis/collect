package org.openforis.idm.metamodel.validation;

public enum ValidationResultFlag {
	OK, ERROR, WARNING;

	public static ValidationResultFlag valueOf(boolean valid) {
		if ( valid ) {
			return OK;
		} else {
			return ERROR;
		}
	}

	public static ValidationResultFlag valueOf(boolean valid, Check.Flag flag) {
		if ( valid ) {
			return OK;
		} else {
			switch (flag) {
			case ERROR:
				return ERROR;
			case WARN:
				return WARNING;
			default:
				throw new UnsupportedOperationException("Unknown flag "+flag);
			}
		}
	}
	
	public boolean isOk() {
		return this == OK;
	}
	
	public boolean isError() {
		return this == ERROR;
	}
	
	public boolean isWarning() {
		return this == WARNING;
	}
}
