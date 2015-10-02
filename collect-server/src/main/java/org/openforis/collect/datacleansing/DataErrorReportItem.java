package org.openforis.collect.datacleansing;


/**
 * 
 * @author A. Modragon
 *
 */
public class DataErrorReportItem extends DataQueryResultItem {
	
	public enum Status {
		PENDING('p'),
		FIXED('f');
		
		public static Status fromCode(char code) {
			for (Status status : values()) {
				if (status.code == code) {
					return status;
				}
			}
			return null;
		}
		
		private char code;

		Status(char code) {
			this.code = code;
		}
		
		public char getCode() {
			return code;
		}
		
	}
	
	private DataErrorReport report;
	private DataErrorQuery errorQuery;
	private Status status;

	public DataErrorReportItem(DataErrorReport report, DataErrorQuery errorQuery) {
		super(errorQuery.getQuery());
		this.errorQuery = errorQuery;
		this.report = report;
		this.status = Status.PENDING;
	}
	
	public DataErrorReport getReport() {
		return report;
	}
	
	public DataErrorQuery getErrorQuery() {
		return errorQuery;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
