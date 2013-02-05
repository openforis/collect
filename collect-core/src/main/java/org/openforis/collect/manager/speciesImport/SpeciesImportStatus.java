package org.openforis.collect.manager.speciesImport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.idm.util.CollectionUtil;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesImportStatus extends ProcessStatus {
	
	private int totalRows;
	private int processedRows;
	private LinkedHashMap<Long, List<TaxonParsingError>> rowToErrors;
	private List<Long> skippedRows;
	
	public SpeciesImportStatus() {
		super();
		processedRows = 0;
		rowToErrors = new LinkedHashMap<Long, List<TaxonParsingError>>();
	}
	
	public void rowProcessed() {
		processedRows ++;
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