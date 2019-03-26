package org.openforis.collect.io.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.RecordFileManager.RecordFileHandle;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummary {
	
	private boolean full = true;
	private Map<Step, Integer> totalPerStep;
	private List<DataImportSummaryItem> recordsToImport;
	private List<DataImportSummaryItem> conflictingRecords;
	private List<FileErrorItem> skippedFileErrors;
	private String surveyName;
	
	public DataImportSummary() {
		this(true);
	}
	
	public DataImportSummary(boolean full) {
		super();
		this.full = full;
	}

	public List<RecordFileHandle> getConflictingRecordFiles() {
		return getConflictingRecordFiles(null);
	}
	
	public List<RecordFileHandle> getConflictingRecordFiles(List<Integer> entryIds) {
		if (! full) {
			return null;
		}
		List<RecordFileHandle> result = new ArrayList<RecordFileHandle>();
		for (DataImportSummaryItem conflictingRecordItem : conflictingRecords) {
			if (entryIds == null || entryIds.contains(conflictingRecordItem.getEntryId())) {
				result.addAll(conflictingRecordItem.getConflictingRecordSummary().getFileHandles());
			}
		}
		return result;
	}
	
	public List<DataImportSummaryItem> getTotalRecords() {
		List<DataImportSummaryItem> items = new ArrayList<DataImportSummaryItem>();
		items.addAll(recordsToImport);
		items.addAll(conflictingRecords);
		return items;
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
