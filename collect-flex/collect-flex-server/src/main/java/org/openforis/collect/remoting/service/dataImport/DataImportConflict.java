package org.openforis.collect.remoting.service.dataImport;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.proxy.RecordProxy;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportConflict {

	private String packagedEntryFileName;
	private RecordProxy existingRecord;
	private RecordProxy importRecord;
	
	public DataImportConflict(CollectRecord existingRecord, CollectRecord importRecord) {
		super();
		this.existingRecord = createSummary(existingRecord);
		this.importRecord = createSummary(importRecord);
	}
	
	public DataImportConflict(String packagedEntryFileName, CollectRecord existingRecord, CollectRecord importRecord) {
		this(existingRecord, importRecord);
		this.packagedEntryFileName = packagedEntryFileName;
	}

	protected RecordProxy createSummary(CollectRecord record) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		String versionName = record.getVersion().getName();
		CollectRecord result = new CollectRecord(survey, versionName);
		result.setCreatedBy(record.getCreatedBy());
		result.setCreationDate(record.getCreationDate());
		result.setEntityCounts(record.getEntityCounts());
		result.setErrors(record.getErrors());
		result.setId(record.getId());
		result.setMissing(record.getMissing());
		result.setModifiedBy(record.getModifiedBy());
		result.setModifiedDate(record.getModifiedDate());
		result.setRootEntityKeyValues(record.getRootEntityKeyValues());
		result.setSkipped(record.getSkipped());
		result.setState(record.getState());
		result.setStep(record.getStep());
		return new RecordProxy(result);
	}
	
	public String getPackagedEntryFileName() {
		return packagedEntryFileName;
	}

	public RecordProxy getExistingRecord() {
		return existingRecord;
	}

	public RecordProxy getImportRecord() {
		return importRecord;
	}

}
