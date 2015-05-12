package org.openforis.collect.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class ReferenceDataImportStatus<E extends ParsingError> extends ProcessStatus {
	
	private Map<Long, List<E>> rowToErrors;
	private List<Long> processedRows;
	
	public ReferenceDataImportStatus() {
		super();
		this.processedRows = new ArrayList<Long>();
		this.rowToErrors = new LinkedHashMap<Long, List<E>>();
	}
	
	public synchronized void addParsingError(long row, E error) {
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
	
	public synchronized void addProcessedRow(long rowNumber) {
		if ( !processedRows.contains(rowNumber)) {
			incrementProcessed();
			processedRows.add(rowNumber);
		}
	}
	
	public synchronized List<E> getErrors() {
		Collection<List<E>> errorsPerRows = rowToErrors.values();
		List<E> result = new ArrayList<E>();
		for (List<E> errros : errorsPerRows) {
			result.addAll(errros);
		}
		return result;
	}
	
	public synchronized boolean hasErrors() {
		return rowToErrors != null && ! rowToErrors.isEmpty();
	}

	public synchronized List<Long> getProcessedRows() {
		return processedRows;
	}
	
	public synchronized boolean isRowProcessed(long rowNumber) {
		return processedRows.contains(rowNumber);
	}
	
	public synchronized boolean isRowInError(long rowNumber) {
		return rowToErrors.containsKey(rowNumber);
	}
	
	public synchronized Collection<Long> getRowsInError() {
		return rowToErrors.keySet();
	}
	
	public synchronized List<Long> getSkippedRows() {
		List<Long> result = new ArrayList<Long>();
		for (long i = 1; i <= getTotal(); i++) {
			if ( ! processedRows.contains(i) ) {
				result.add(i);
			}
		}
		return CollectionUtils.unmodifiableList(result);
	}

}