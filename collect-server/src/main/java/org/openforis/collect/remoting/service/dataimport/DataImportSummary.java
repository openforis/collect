package org.openforis.collect.remoting.service.dataimport;

import java.util.List;
import java.util.Map;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.persistence.xml.DataHandler.NodeUnmarshallingError;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummary {
	
	private Map<Step, Integer> totalPerStep;
	private List<DataImportSummaryItem> recordsToImport;
	private List<DataImportSummaryItem> conflictingRecords;
	private List<FileErrorItem> skippedFileErrors;
	private String surveyName;
	
	public DataImportSummary() {
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

	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}
	
	public List<FileErrorItem> getSkippedFileErrors() {
		return skippedFileErrors;
	}

	public void setSkippedFileErrors(List<FileErrorItem> skippedFileErrors) {
		this.skippedFileErrors = skippedFileErrors;
	}

	public static class FileErrorItem {
		
		private String fileName;
		private List<NodeUnmarshallingError> errors;
		
		public FileErrorItem(String fileName, List<NodeUnmarshallingError> errors) {
			super();
			this.fileName = fileName;
			this.errors = errors;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public List<NodeUnmarshallingError> getErrors() {
			return errors;
		}

		public void setErrors(List<NodeUnmarshallingError> errors) {
			this.errors = errors;
		}
		
		
		
	}

}
