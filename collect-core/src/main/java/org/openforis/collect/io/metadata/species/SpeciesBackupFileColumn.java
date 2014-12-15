package org.openforis.collect.io.metadata.species;

/**
 * 
 * @author S. Ricci
 *
 */
public enum SpeciesBackupFileColumn {
	ID("id"), PARENT_ID("parent_id"), RANK("rank"), NO("no"), CODE("code"), SCIENTIFIC_NAME("scientific_name"), SYNONYMS("synonyms");

	public static final SpeciesBackupFileColumn[] REQUIRED_COLUMNS = {ID, PARENT_ID, RANK, CODE, SCIENTIFIC_NAME};
	
	public static final String[] REQUIRED_COLUMN_NAMES;
	static {
		String[] names = new String[REQUIRED_COLUMNS.length];
		for (int i = 0; i < REQUIRED_COLUMNS.length; i++) {
			SpeciesBackupFileColumn col = REQUIRED_COLUMNS[i];
			names[i] = col.columnName;
		}
		REQUIRED_COLUMN_NAMES = names;
	}
	
	private String columnName;
	
	private SpeciesBackupFileColumn(String columnName) {
		this.columnName = columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}
}