package org.openforis.collect.metamodel.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonSummariesProxy implements Proxy {

	private transient TaxonSummaries taxonSummaries;

	public TaxonSummariesProxy(TaxonSummaries taxonSummaries) {
		super();
		this.taxonSummaries = taxonSummaries;
	}
	
	@ExternalizedProperty
	public int getTotalCount() {
		return taxonSummaries.getTotalCount();
	}

	@ExternalizedProperty
	public List<TaxonSummaryProxy> getSummaries() {
		List<TaxonSummary> summaries = taxonSummaries.getItems();
		return TaxonSummaryProxy.fromList(summaries);
	}

	@ExternalizedProperty
	public List<String> getVernacularNamesLanguageCodes() {
		return taxonSummaries.getVernacularNamesLanguageCodes();
	}
	
	@ExternalizedProperty
	public List<String> getInfoAttributes() {
		return taxonSummaries.getInfoAttributeNames();
	}
}
