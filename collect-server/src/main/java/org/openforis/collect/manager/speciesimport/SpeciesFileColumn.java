package org.openforis.collect.manager.speciesimport;

/**
 * 
 * @author S. Ricci
 *
 */
public enum SpeciesFileColumn {
	NO("no"), CODE("code"), FAMILY("family"), SCIENTIFIC_NAME("scientific_name"), SYNONYMS("synonyms");

	public static final SpeciesFileColumn[] REQUIRED_COLUMNS = {CODE, FAMILY, SCIENTIFIC_NAME};
	
	public static final String[] REQUIRED_COLUMN_NAMES = new String[] {
		CODE.getColumnName(), FAMILY.getColumnName(), SCIENTIFIC_NAME.getColumnName()
	};
	
	private String columnName;
	
	private SpeciesFileColumn(String columnName) {
		this.columnName = columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}
}