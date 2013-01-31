package org.openforis.collect.manager.speciesImport;

/**
 * 
 * @author S. Ricci
 *
 */
public enum TaxonFileColumn {
	ID(0, "no"), CODE(1, "code"), FAMILY(2, "family"), SCIENTIFIC_NAME(3, "scientific_name");
	
	private int index;
	private String name;
	
	private TaxonFileColumn(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getName() {
		return name;
	}
}
