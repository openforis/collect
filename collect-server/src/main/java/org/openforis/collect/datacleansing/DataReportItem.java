package org.openforis.collect.datacleansing;


/**
 * 
 * @author A. Modragon
 *
 */
public class DataReportItem extends DataQueryResultItem {
	
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
	
	private DataReport report;
	private DataQuery query;
	private Status status;

	public DataReportItem(DataReport report, DataQuery query) {
		super(query);
		this.query = query;
		this.report = report;
		this.status = Status.PENDING;
	}
	
	public DataReport getReport() {
		return report;
	}
	
	public DataQuery getQuery() {
		return query;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
