package org.openforis.collect.manager.samplingdesignimport;

/**
 * 
 * @author S. Ricci
 *
 */
public enum SamplingDesignFileColumn {
	LEVEL_1("level1_code"), 
	LEVEL_2("level2_code"), 
	LEVEL_3("level3_code"), 
	X("x"),
	Y("y"),
	SRS_ID("srs_id");
	
	public static final SamplingDesignFileColumn[] LOCATION_COLUMNS = {X, Y};
	public static final SamplingDesignFileColumn[] REQUIRED_COLUMNS = {LEVEL_1, X, Y, SRS_ID};
	
	public static final String[] REQUIRED_COLUMN_NAMES;
	static {
		String [] requiredColNames = new String[REQUIRED_COLUMNS.length];
		for (int i = 0 ; i < REQUIRED_COLUMNS.length; i ++) {
			SamplingDesignFileColumn column = REQUIRED_COLUMNS[i];
			requiredColNames[i] = column.getColumnName();
		}
		REQUIRED_COLUMN_NAMES = requiredColNames;
	}
	
	private String columnName;
	
	private SamplingDesignFileColumn(String columnName) {
		this.columnName = columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}
}