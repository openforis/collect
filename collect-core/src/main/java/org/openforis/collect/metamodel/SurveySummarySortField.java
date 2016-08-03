package org.openforis.collect.metamodel;

/**
 * @author S. Ricci
 *
 */
public class SurveySummarySortField {

	public enum Sortable {
		NAME("name"),
		PROJECT_NAME("projectName"),
		MODIFIED_DATE("modifiedDate"),
		TARGET("target"),
		MODIFIED("temporary"),
		PUBLISHED("published");

		private String fieldName;
		
		private Sortable(String fieldName) {
			this.fieldName = fieldName;
		}
		
		public String getFieldName() {
			return fieldName;
		}
	}
	
	private Sortable field;
	private boolean descending;
	
	public SurveySummarySortField(Sortable field) {
		super();
		this.field = field;
	}

	public SurveySummarySortField(Sortable field, boolean descending) {
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
