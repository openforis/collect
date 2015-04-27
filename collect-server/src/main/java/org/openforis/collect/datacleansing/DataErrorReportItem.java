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
	private Status status;

	public DataErrorReportItem(DataErrorReport report) {
		super(report.getQuery().getQuery());
		this.report = report;
	}
	
	public DataErrorReport getReport() {
		return report;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
