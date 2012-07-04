package org.openforis.collect.remoting.service.dataImport;

import java.util.List;
import java.util.Map;

import org.openforis.collect.model.CollectRecord.Step;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummary {
	
	private Map<Step, Integer> totalPerStep;
	private List<DataImportSummaryItem> recordsToImport;
	private List<DataImportSummaryItem> conflictingRecords;
	private Map<String, String> skippedFileErrors;
	private boolean newSurvey;
	
	public DataImportSummary() {
		newSurvey = true;
	}

	public Map<String, String> getSkippedFileErrors() {
		return skippedFileErrors;
	}

	public void setSkippedFileErrors(Map<String, String> skippedFileErrors) {
		this.skippedFileErrors = skippedFileErrors;
	}

	public boolean isNewSurvey() {
		return newSurvey;
	}

	public void setNewSurvey(boolean newSurvey) {
		this.newSurvey = newSurvey;
	}

	public Map<Step, Integer> getTotalPerStep() {
		return totalPerStep;
	}

	public void setTotalPerStep(Map<Step, Integer> totalPerStep) {
		this.totalPerStep = totalPerStep;
	}

	public List<DataImportSummaryItem> getRecordsToImport() {
		return recordsToImport;
	}

	public void setRecordsToImport(List<DataImportSummaryItem> recordsToImport) {
		this.recordsToImport = recordsToImport;
	}

	public List<DataImportSummaryItem> getConflictingRecords() {
		return conflictingRecords;
	}

	public void setConflictingRecords(List<DataImportSummaryItem> conflictingRecords) {
		this.conflictingRecords = conflictingRecords;
	}

	

}
