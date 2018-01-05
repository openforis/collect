package org.openforis.collect.io.data;

public class RecordProviderConfiguration {

	private boolean createUsersFoundInRecords;
	
	public RecordProviderConfiguration() {
	}
	
	public RecordProviderConfiguration(boolean createUsersFoundInRecords) {
		super();
		this.createUsersFoundInRecords = createUsersFoundInRecords;
	}

	public boolean isCreateUsersFoundInRecords() {
		return createUsersFoundInRecords;
	}
	
	public void setCreateUsersFoundInRecords(boolean createUsersFoundInRecords) {
		this.createUsersFoundInRecords = createUsersFoundInRecords;
	}
}
