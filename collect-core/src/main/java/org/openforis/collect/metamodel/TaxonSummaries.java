package org.openforis.collect.metamodel;

import java.util.List;
import java.util.Set;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonSummaries {

	private int totalCount;
	private List<TaxonSummary> items;
	private Set<String> vernacularNamesLanguageCodes;
	
	public TaxonSummaries(int totalCount, List<TaxonSummary> items, Set<String> vernacularNamesLanguageCodes) {
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

	public Set<String> getVernacularNamesLanguageCodes() {
		return vernacularNamesLanguageCodes;
	}

	public void setVernacularNamesLanguageCodes(Set<String> vernacularNamesLanguageCodes) {
		this.vernacularNamesLanguageCodes = vernacularNamesLanguageCodes;
	}
	
}
