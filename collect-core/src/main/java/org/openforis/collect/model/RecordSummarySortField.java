package org.openforis.collect.model;

/**
 * @author S. Ricci
 *
 */
public class RecordSummarySortField {

	public enum Sortable {
		KEY1, KEY2, KEY3, 
		COUNT1, COUNT2, COUNT3,
		SUMMARY1, SUMMARY2, SUMMARY3, 
		SKIPPED, MISSING, ERRORS, WARNINGS, 
		DATE_CREATED, DATE_MODIFIED, 
		OWNER_NAME,
		STEP
	}
	
	private Sortable field;
	private boolean descending;
	
	public RecordSummarySortField() {
	}
	
	public RecordSummarySortField(Sortable field) {
		super();
		this.field = field;
	}

	public RecordSummarySortField(Sortable field, boolean descending) {
		super();
		this.field = field;
		this.descending = descending;
	}

	public Sortable getField() {
		return field;
	}

	public void setField(Sortable field) {
		this.field = field;
	}

	public boolean isDescending() {
		return descending;
	}
	
	public void setDescending(boolean descending) {
		this.descending = descending;
	}
	
}
