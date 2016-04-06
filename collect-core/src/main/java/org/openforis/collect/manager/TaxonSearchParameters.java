package org.openforis.collect.manager;

public class TaxonSearchParameters {
	
	private boolean includeUniqueVernacularName;
	private boolean includeAncestorTaxons;
	
	public boolean isIncludeUniqueVernacularName() {
		return includeUniqueVernacularName;
	}
	
	public void setIncludeUniqueVernacularName(boolean includeUniqueVernacularName) {
		this.includeUniqueVernacularName = includeUniqueVernacularName;
	}
	
	public boolean isIncludeAncestorTaxons() {
		return includeAncestorTaxons;
	}
	
	public void setIncludeAncestorTaxons(boolean includeAncestorTaxons) {
		this.includeAncestorTaxons = includeAncestorTaxons;
	} 
}