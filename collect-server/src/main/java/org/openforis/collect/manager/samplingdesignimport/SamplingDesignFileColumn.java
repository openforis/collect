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
	public static final SamplingDesignFileColumn[] LEVEL_COLUMNS = {LEVEL_1, LEVEL_2, LEVEL_3};
	
	public static final String[] REQUIRED_COLUMN_NAMES;
	public static final String[] LEVEL_COLUMN_NAMES;
	static {
		REQUIRED_COLUMN_NAMES = getColumnNames(REQUIRED_COLUMNS);
		LEVEL_COLUMN_NAMES = getColumnNames(LEVEL_COLUMNS);
	}
	
	private String columnName;
	
	private static String[] getColumnNames(SamplingDesignFileColumn[] columns) {
		String [] result = new String[columns.length];
		for (int i = 0 ; i < columns.length; i ++) {
			SamplingDesignFileColumn column = columns[i];
			result[i] = column.getColumnName();
		}
		return result;
	}
	
	private SamplingDesignFileColumn(String columnName) {
		this.columnName = columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}
}