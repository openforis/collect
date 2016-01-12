package org.openforis.collect.io.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.concurrency.Task;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class ReferenceDataImportTask<E extends ParsingError> extends Task {

	private Map<Long, List<E>> rowToErrors;
	private List<Long> processedRows;
	
	public ReferenceDataImportTask() {
		this.processedRows = new ArrayList<Long>();
		this.rowToErrors = new LinkedHashMap<Long, List<E>>();
	}
	
	public void addParsingError(long row, E error) {
		List<E> list = rowToErrors.get(row);
		if ( list == null ) {
			list = new ArrayList<E>();
			rowToErrors.put(row, list);
		}
		if ( ! list.contains(error) ) {
			list.add(error);
		}
	}
	
	public void addParsingError(E error) {
		long row = error.getRow();
		if ( row > 0 ) {
			addParsingError(row, error);
		} else {
			throw new IllegalArgumentException("Error row must be greater than zero");
		}
	}
	
	public void addProcessedRow(long rowNumber) {
		if ( !processedRows.contains(rowNumber)) {
			incrementProcessedItems();
			processedRows.add(rowNumber);
		}
	}
	
	public List<E> getErrors() {
		Collection<List<E>> errorsPerRows = rowToErrors.values();
		List<E> result = new ArrayList<E>();
		for (List<E> errros : errorsPerRows) {
			result.addAll(errros);
		}
		return result;
	}
	
	public boolean hasErrors() {
		return rowToErrors != null && ! rowToErrors.isEmpty();
	}

	public List<Long> getProcessedRows() {
		return processedRows;
	}
	
	public boolean isRowProcessed(long rowNumber) {
		return processedRows.contains(rowNumber);
	}
	
	public boolean isRowInError(long rowNumber) {
		return rowToErrors.containsKey(rowNumber);
	}
	
	public Collection<Long> getRowsInError() {
		return rowToErrors.keySet();
	}
	
	public List<Long> getSkippedRows() {
		List<Long> result = new ArrayList<Long>();
		for (long i = 1; i <= getTotalItems(); i++) {
			if ( ! processedRows.contains(i) ) {
				result.add(i);
			}
		}
		return CollectionUtils.unmodifiableList(result);
	}
}
