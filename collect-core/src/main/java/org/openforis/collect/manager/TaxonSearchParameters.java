package org.openforis.collect.manager;

import org.openforis.idm.model.species.Taxon.TaxonRank;

public class TaxonSearchParameters {
	
	private TaxonRank highestRank = TaxonRank.FAMILY;
	private boolean includeUniqueVernacularName;
	private boolean includeAncestorTaxons;
	
	public TaxonRank getHighestRank() {
		return highestRank;
	}
	
	public void setHighestRank(TaxonRank highestRank) {
		this.highestRank = highestRank;
	}
	
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