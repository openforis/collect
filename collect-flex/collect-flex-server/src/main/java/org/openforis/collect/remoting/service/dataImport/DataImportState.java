package org.openforis.collect.remoting.service.dataImport;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.remoting.service.dataProcessing.DataProcessingState;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportState extends DataProcessingState {

	private Map<String, String> errors;
	private Map<String, String> warnings;
	
	private Map<Step, Integer> totalPerStep;
	
	private int insertedCount;
	private int updatedCount;
	private DataImportConflict conflict;

	public DataImportState() {
		super();
		insertedCount = 0;
		updatedCount = 0;
		errors = new HashMap<String, String>();
		warnings = new HashMap<String, String>();
	}

	public void addError(String fileName, String error) {
		errors.put(fileName, error);
	}

	public void addWarning(String fileName, String warning) {
		warnings.put(fileName, warning);
	}

	public Map<Step, Integer> getTotalPerStep() {
		return totalPerStep;
	}

	public void setTotalPerStep(Map<Step, Integer> totalPerStep) {
		this.totalPerStep = totalPerStep;
	}
	
	public void incrementInsertedCount() {
		insertedCount ++;
		incrementCount();
	}
	
	public void incrementUpdatedCount() {
		updatedCount ++;
		incrementCount();
	}

	public int getInsertedCount() {
		return insertedCount;
	}

	public int getUpdatedCount() {
		return updatedCount;
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public Map<String, String> getWarnings() {
		return warnings;
	}

	public DataImportConflict getConflict() {
		return conflict;
	}
	
	public void setConflict(DataImportConflict conflict) {
		this.conflict = conflict;
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
