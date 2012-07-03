package org.openforis.collect.remoting.service.dataImport;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.dataProcessing.DataProcessingState;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportState extends DataProcessingState {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Step {
		PREPARE, INITED, STARTING, IMPORTING, CONFLICT, COMPLETE, CANCELLED, ERROR;
	}
	
	private Step step;
	private boolean newSurvey;
	private Map<String, String> errors;
	private Map<String, String> warnings;
	
	private Map<CollectRecord.Step, Integer> totalPerStep;
	
	private int insertedCount;
	private int updatedCount;
	private DataImportConflict conflict;
	private int conflictingEntryRecordId;

	public DataImportState() {
		super();
		insertedCount = 0;
		updatedCount = 0;
		errors = new HashMap<String, String>();
		warnings = new HashMap<String, String>();
		//totalPerStep = new HashMap<CollectRecord.Step, Integer>();
		step = Step.ERROR;
	}

	public void addError(String fileName, String error) {
		errors.put(fileName, error);
	}

	public void addWarning(String fileName, String warning) {
		warnings.put(fileName, warning);
	}
	
	public Map<CollectRecord.Step, Integer> getTotalPerStep() {
		return totalPerStep;
	}

	public void setTotalPerStep(Map<CollectRecord.Step, Integer> totalPerStep) {
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

	public boolean isNewSurvey() {
		return newSurvey;
	}

	public void setNewSurvey(boolean newSurvey) {
		this.newSurvey = newSurvey;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public int getConflictingEntryRecordId() {
		return conflictingEntryRecordId;
	}

	public void setConflictingEntryRecordId(int conflictingEntryRecordId) {
		this.conflictingEntryRecordId = conflictingEntryRecordId;
	}

}
