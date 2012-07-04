package org.openforis.collect.remoting.service.dataImport;

import java.util.HashMap;
import java.util.Map;

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
		PREPARE, INITED, STARTING, IMPORTING, COMPLETE, CANCELLED, ERROR;
	}
	
	private Step step;
	private Map<String, String> errors;
	private Map<String, String> warnings;
	
	private int insertedCount;
	private int updatedCount;

	public DataImportState() {
		super();
		insertedCount = 0;
		updatedCount = 0;
		errors = new HashMap<String, String>();
		warnings = new HashMap<String, String>();
		step = Step.PREPARE;
	}

	public void addError(String fileName, String error) {
		errors.put(fileName, error);
	}

	public void addWarning(String fileName, String warning) {
		warnings.put(fileName, warning);
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

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

}
