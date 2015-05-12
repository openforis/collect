package org.openforis.collect.model;

import java.util.List;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractSummaries<T> {
	
	private int totalCount;
	private List<T> records;
	
	public AbstractSummaries(int totalCount, List<T> records) {
		super();
		this.totalCount = totalCount;
		this.records = records;
	}

	public int getTotalCount() {
		return totalCount;
	}
	
	public List<T> getRecords() {
		return records;
	}
	
}
