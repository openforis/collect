/**
 * 
 */
package org.openforis.collect.io.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.concurrency.Task;

/**
 * @author S. Ricci
 *
 */
public abstract class ReferenceDataImportTask<E extends ParsingError> extends Task {

	private Map<Long, List<E>> rowToErrors;
	private Set<Long> processedRows;
	
	public ReferenceDataImportTask() {
		super();
		this.processedRows = new TreeSet<Long>();
		this.rowToErrors = new TreeMap<Long, List<E>>();
	}
	
	protected synchronized void addParsingError(long row, E error) {
		List<E> list = rowToErrors.get(row);
		if ( list == null ) {
			list = new ArrayList<E>();
			rowToErrors.put(row, list);
		}
		if ( ! list.contains(error) ) {
			list.add(error);
		}
	}
	
	protected void addParsingError(E error) {
		long row = error.getRow();
		if ( row > 0 ) {
			addParsingError(row, error);
		} else {
			throw new IllegalArgumentException("Error row must be greater than zero");
		}
	}
	
	protected synchronized void addProcessedRow(long rowNumber) {
		incrementProcessedItems();
		processedRows.add(rowNumber);
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
		return new ArrayList<Long>(processedRows);
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
		for (long i = 1; i <= getTotalItems(); i++) {
			if ( ! processedRows.contains(i) ) {
				result.add(i);
			}
		}
		return CollectionUtils.unmodifiableList(result);
	}

}
