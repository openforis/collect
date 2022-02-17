package org.openforis.collect.io.metadata.species;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author S. Ricci
 *
 */
public enum SpeciesFileColumn {
	NO("no"), CODE("code"), FAMILY("family"), SCIENTIFIC_NAME("scientific_name"), SYNONYMS("synonyms");

	public static final String[] PREDEFINED_COLUMN_NAMES = getColumnNames(values());

	public static final SpeciesFileColumn[] REQUIRED_COLUMNS = {CODE, FAMILY, SCIENTIFIC_NAME};
	
	public static final String[] REQUIRED_COLUMN_NAMES = getColumnNames(REQUIRED_COLUMNS);
		
	private static String[] getColumnNames(SpeciesFileColumn[] columns) {
		List<String> colNames = new ArrayList<String>(columns.length);
		for (SpeciesFileColumn col : columns) {
			colNames.add(col.getColumnName());
		}
		return colNames.toArray(new String[0]);
	}
	
	private String columnName;
	
	private SpeciesFileColumn(String columnName) {
		this.columnName = columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}
}