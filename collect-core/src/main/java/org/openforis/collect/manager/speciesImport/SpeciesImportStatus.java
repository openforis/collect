package org.openforis.collect.manager.speciesImport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.openforis.idm.util.CollectionUtil;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesImportStatus {
	
	enum Step {
		INITED, PREPARING, RUNNING, COMPLETE, CANCELLED, ERROR
	}
	
	private SpeciesImportStatus.Step step;
	private int totalRows;
	private int processedRows;
	private LinkedHashMap<Long, List<TaxonParsingError>> rowToErrors;
	private List<Long> skippedRows;
	
	public SpeciesImportStatus() {
		step = Step.INITED;
		processedRows = 0;
		rowToErrors = new LinkedHashMap<Long, List<TaxonParsingError>>();
	}
	
	public void rowProcessed() {
		processedRows ++;
	}
	
	public SpeciesImportStatus.Step getStep() {
		return step;
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

	public void addError(long row, TaxonParsingError error) {
		List<TaxonParsingError> list = rowToErrors.get(row);
		if ( list == null ) {
			list = new ArrayList<TaxonParsingError>();
			rowToErrors.put(row, list);
		}
		if ( ! list.contains(error) ) {
			list.add(error);
		}
	}
	
	public List<TaxonParsingError> getErrors() {
		Collection<List<TaxonParsingError>> errorsPerRows = rowToErrors.values();
		List<TaxonParsingError> result = new ArrayList<TaxonParsingError>();
		for (List<TaxonParsingError> errros : errorsPerRows) {
			result.addAll(errros);
		}
		return result;
	}
	
	public boolean isRunning() {
		return step == Step.RUNNING;
	}
	
	public boolean isComplete() {
		return step == Step.COMPLETE;
	}
	
	public boolean hasErrors() {
		return rowToErrors != null && ! rowToErrors.isEmpty();
	}
	
	public int getProcessedRows() {
		return processedRows;
	}
	
	public int getTotalRows() {
		return totalRows;
	}
	
	public List<Long> getSkippedRows() {
		return CollectionUtil.unmodifiableList(skippedRows);
	}

}