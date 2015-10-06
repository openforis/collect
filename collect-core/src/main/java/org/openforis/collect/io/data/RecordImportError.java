package org.openforis.collect.io.data;

import org.openforis.collect.model.CollectRecord.Step;

public class RecordImportError {

	public enum Level {
		WARNING, ERROR;
	}
	
	private int entryId;
	private String entryName;
	private Step recordStep;
	private String errorMessage;
	private Level level;
	
	public RecordImportError(int entryId, String entryName, Step recordStep,
			String errorMessage, Level level) {
		super();
		this.entryId = entryId;
		this.entryName = entryName;
		this.recordStep = recordStep;
		this.errorMessage = errorMessage;
		this.level = level;
	}

	public int getEntryId() {
		return entryId;
	}

	public String getEntryName() {
		return entryName;
	}

	public Step getRecordStep() {
		return recordStep;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Level getLevel() {
		return level;
	}
	
}
