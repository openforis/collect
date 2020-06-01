package org.openforis.collect.io.metadata.samplingdesign;

import java.util.Arrays;

/**
 * 
 * @author S. Ricci
 *
 */
public enum SamplingDesignFileColumn {
	LEVEL_1("level1_code", 1), 
	LEVEL_2("level2_code", 2), 
	LEVEL_3("level3_code", 3), 
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
	
	public static SamplingDesignFileColumn fromColumnName(String columnName) {
		for (SamplingDesignFileColumn column : values()) {
			if (column.getColumnName().equals(columnName)) {
				return column;
			}
		}
		return null;
	}

	private static String[] getColumnNames(SamplingDesignFileColumn[] columns) {
		String [] result = new String[columns.length];
		for (int i = 0 ; i < columns.length; i ++) {
			SamplingDesignFileColumn column = columns[i];
			result[i] = column.getColumnName();
		}
		return result;
	}
	
	private String columnName;
	private int level;
	
	private SamplingDesignFileColumn(String columnName, int level) {
		this.columnName = columnName;
		this.level = level;
	}
	
	private SamplingDesignFileColumn(String columnName) {
		this(columnName, -1);
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public int getLevel() {
		return level;
	}
	
	public boolean isLevelColumn() {
		return Arrays.asList(LEVEL_COLUMNS).contains(this);
	}
	
}