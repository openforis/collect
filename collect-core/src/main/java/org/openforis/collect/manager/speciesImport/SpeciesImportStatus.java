package org.openforis.collect.manager.speciesImport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.openforis.idm.util.CollectionUtil;

public class SpeciesImportStatus {
	
	enum Step {
		INITED, PREPARING, RUNNING, COMPLETE, CANCELLED, ERROR
	}
	
	private SpeciesImportStatus.Step step;
	private int totalRows;
	private int processedRows;
	private LinkedHashMap<Integer, TaxonParsingError> rowToError;
	private List<Integer> skippedRows;
	
	public SpeciesImportStatus() {
		step = Step.INITED;
		processedRows = 0;
		rowToError = new LinkedHashMap<Integer, TaxonParsingError>();
	}
	
	public void rowProcessed() {
		processedRows ++;
	}
	
	public void error() {
		step = Step.ERROR;			
	}

	public void complete() {
		step = Step.COMPLETE;
	}

	public void start() {
		step = Step.RUNNING;
	}

	public void cancel() {
		step = Step.CANCELLED;
	}

	public void addError(int row, TaxonParsingError error) {
		rowToError.put(row, error);
	}
	
	public List<TaxonParsingError> getErrors() {
		return new ArrayList<TaxonParsingError>(rowToError.values());
	}
	
	public boolean isRunning() {
		return step == Step.RUNNING;
	}
	
	public boolean isComplete() {
		return step == Step.COMPLETE;
	}
	
	public int getProcessedRows() {
		return processedRows;
	}
	
	public int getTotalRows() {
		return totalRows;
	}
	
	public List<Integer> getSkippedRows() {
		return CollectionUtil.unmodifiableList(skippedRows);
	}
	
}