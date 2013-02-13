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
	
	public TaxonSummaries(int totalCount, List<TaxonSummary> items, List<String> vernacularNamesLanguageCodes) {
		super();
		this.totalCount = totalCount;
		this.items = items;
		this.vernacularNamesLanguageCodes = vernacularNamesLanguageCodes;
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

}
