package org.openforis.collect.manager.speciesImport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.idm.util.CollectionUtil;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesImportStatus extends ProcessStatus {
	
	private Map<Long, List<TaxonParsingError>> rowToErrors;
	private List<Long> processedRows;
	private String taxonomyName;
	
	public SpeciesImportStatus(String taxonomyName) {
		super();
		this.taxonomyName = taxonomyName;
		this.processedRows = new ArrayList<Long>();
		this.rowToErrors = new LinkedHashMap<Long, List<TaxonParsingError>>();
	}
	
	public void addParsingError(long row, TaxonParsingError error) {
		List<TaxonParsingError> list = rowToErrors.get(row);
		if ( list == null ) {
			list = new ArrayList<TaxonParsingError>();
			rowToErrors.put(row, list);
		}
		if ( ! list.contains(error) ) {
			list.add(error);
		}
	}
	
	public void addProcessRow(long rowNumber) {
		if ( !processedRows.contains(rowNumber)) {
			incrementProcessed();
			processedRows.add(rowNumber);
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

	public String getTaxonomyName() {
		return taxonomyName;
	}
	
	public List<Long> getSkippedRows() {
		List<Long> result = new ArrayList<Long>();
		for (long i = 1; i <= getTotal(); i++) {
			if ( ! processedRows.contains(i) ) {
				result.add(i);
			}
		}
		return CollectionUtil.unmodifiableList(result);
	}

}