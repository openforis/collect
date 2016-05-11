package org.openforis.collect.io.data.csv;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVDataImportSettings implements Cloneable {
	
	/**
	 * If true, records are validated after insert or update
	 */
	private boolean recordValidationEnabled;
	/**
	 * If true, only new records will be inserted and only root entities can be added
	 */
	private boolean insertNewRecords;
	/**
	 * When insertNewRecords is true, it indicates the name of the model version used during new record creation
	 */
	private String newRecordVersionName;
	
	/**
	 * If true, the process automatically creates ancestor multiple entities if they do not exist.
	 */
	private boolean createAncestorEntities;
	
	/**
	 * If true, the process automatically delete all entities with the specified definition id before importing the new ones.
	 */
	private boolean deleteExistingEntities;
	
	/**
	 * If true, only existing records update is allowed. 
	 */
	private boolean reportNoRecordFoundErrors;
	
	public CSVDataImportSettings() {
		recordValidationEnabled = true;
		insertNewRecords = false;
		deleteExistingEntities = false;
		reportNoRecordFoundErrors = true;
	}
	
	@Override
	public CSVDataImportSettings clone() throws CloneNotSupportedException {
		return (CSVDataImportSettings) super.clone();
	}
	
	public boolean isRecordValidationEnabled() {
		return recordValidationEnabled;
	}
	
	public void setRecordValidationEnabled(boolean recordValidationEnabled) {
		this.recordValidationEnabled = recordValidationEnabled;
	}

	public boolean isInsertNewRecords() {
		return insertNewRecords;
	}
	
	public void setInsertNewRecords(boolean insertNewRecords) {
		this.insertNewRecords = insertNewRecords;
	}
	
	public String getNewRecordVersionName() {
		return newRecordVersionName;
	}
	
	public void setNewRecordVersionName(String newRecordVersionName) {
		this.newRecordVersionName = newRecordVersionName;
	}
	
	public boolean isCreateAncestorEntities() {
		return createAncestorEntities;
	}
	
	public void setCreateAncestorEntities(boolean createAncestorEntities) {
		this.createAncestorEntities = createAncestorEntities;
	}
	
	public boolean isDeleteExistingEntities() {
		return deleteExistingEntities;
	}
	
	public void setDeleteExistingEntities(boolean deleteExistingEntities) {
		this.deleteExistingEntities = deleteExistingEntities;
	}
	
	public boolean isReportNoRecordFoundErrors() {
		return reportNoRecordFoundErrors;
	}
	
	public void setReportNoRecordFoundErrors(boolean reportNoRecordFoundErrors) {
		this.reportNoRecordFoundErrors = reportNoRecordFoundErrors;
	}
}