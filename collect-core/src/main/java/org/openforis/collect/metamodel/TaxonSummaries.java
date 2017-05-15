package org.openforis.collect.metamodel;

import java.util.List;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonSummaries {

	private int totalCount;
	private List<TaxonSummary> items;
	private List<String> vernacularNamesLanguageCodes;
	private List<String> infoAttributeNames;
	
	public TaxonSummaries(int totalCount, List<TaxonSummary> items, 
			List<String> vernacularNamesLanguageCodes, List<String> infoAttributeNames) {
		super();
		this.totalCount = totalCount;
		this.items = items;
		this.vernacularNamesLanguageCodes = vernacularNamesLanguageCodes;
		this.infoAttributeNames = infoAttributeNames;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public List<TaxonSummary> getItems() {
		return items;
	}

	public List<String> getVernacularNamesLanguageCodes() {
		return vernacularNamesLanguageCodes;
	}
	
	public List<String> getInfoAttributeNames() {
		return infoAttributeNames;
	}

}
