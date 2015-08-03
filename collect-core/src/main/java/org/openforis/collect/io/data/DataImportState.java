package org.openforis.collect.io.data;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.manager.process.DataProcessingState;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportState extends DataProcessingState{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum MainStep {
		INITED, SUMMARY_CREATION, IMPORT;
	}
	
	public enum SubStep {
		INITED, PREPARING, RUNNING, COMPLETE, CANCELLED, ERROR;
	}
	
	private MainStep mainStep;
	private SubStep subStep;
	private Map<String, String> errors;
	private Map<String, Map<String, String>> warnings;
	
	private int insertedCount;
	private int updatedCount;

	public DataImportState() {
		super();
		insertedCount = 0;
		updatedCount = 0;
		errors = new HashMap<String, String>();
		warnings = new HashMap<String, Map<String, String>>();
		mainStep = MainStep.INITED;
		subStep = SubStep.INITED;
	}

	public void addError(String fileName, String error) {
		errors.put(fileName, error);
	}

	public void addWarnings(String entryName, Map<String, String> warnings) {
		this.warnings.put(entryName, warnings);
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

	public MainStep getMainStep() {
		return mainStep;
	}

	public void setMainStep(MainStep mainStep) {
		this.mainStep = mainStep;
	}

	public SubStep getSubStep() {
		return subStep;
	}

	public void setSubStep(SubStep subStep) {
		this.subStep = subStep;
	}

	public Map<String, Map<String, String>> getWarnings() {
		return warnings;
	}

}
