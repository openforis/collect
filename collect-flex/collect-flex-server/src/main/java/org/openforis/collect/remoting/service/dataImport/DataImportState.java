package org.openforis.collect.remoting.service.dataImport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.CollectRecord.Step;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportState extends DataProcessingState {

	private List<String> skippedFileNames;
	private Map<String, String> errors;
	private Map<String, String> warnings;
	
	private Map<Step, Integer> totalRecords;
	
	public DataImportState() {
		super();
		skippedFileNames = new ArrayList<String>();
		errors = new HashMap<String, String>();
		warnings = new HashMap<String, String>();
	}

	public void addSkipped(String entryName) {
		skippedFileNames.add(entryName);
	}
	
	public void addError(String fileName, String error) {
		errors.put(fileName, error);
	}

	public void addWarning(String fileName, String warning) {
		warnings.put(fileName, warning);
	}

	public List<String> getSkippedFileNames() {
		return skippedFileNames;
	}
	
//	public class RecordEntry {
//		
//		private Step step;
//		private int recordId;
//		private String recordKeys;
//		
//		public RecordEntry(Step step, int recordId, String recordKeys) {
//			super();
//			this.step = step;
//			this.recordId = recordId;
//			this.recordKeys = recordKeys;
//		}
//
//		public Step getStep() {
//			return step;
//		}
//
//		public int getRecordId() {
//			return recordId;
//		}
//
//		public String getRecordKeys() {
//			return recordKeys;
//		}
//		
//		
//		
//	}
//	
}
