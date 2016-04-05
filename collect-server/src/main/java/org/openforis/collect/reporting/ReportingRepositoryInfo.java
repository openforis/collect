package org.openforis.collect.reporting;

import java.util.Date;

public class ReportingRepositoryInfo {

	private Date lastUpdate;
	private int updatedRecordsSinceLastUpdate;

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public int getUpdatedRecordsSinceLastUpdate() {
		return updatedRecordsSinceLastUpdate;
	}

	public void setUpdatedRecordsSinceLastUpdate(int updatedRecordsSinceLastUpdate) {
		this.updatedRecordsSinceLastUpdate = updatedRecordsSinceLastUpdate;
	}
}
