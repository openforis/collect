/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class TaxonSummaryProxy implements Proxy {

	private transient TaxonSummary summary;

	public TaxonSummaryProxy(TaxonSummary summary) {
		super();
		this.summary = summary;
	}

	public static List<TaxonSummaryProxy> fromList(List<TaxonSummary> list) {
		List<TaxonSummaryProxy> proxies = new ArrayList<TaxonSummaryProxy>();
		if (list != null) {
			for (TaxonSummary item : list) {
				TaxonSummaryProxy proxy = new TaxonSummaryProxy(item);
				proxies.add(proxy);
			}
		}
		return proxies;
	}
	
	@ExternalizedProperty
	public String getCode() {
		return summary.getCode();
	}

	@ExternalizedProperty
	public Map<String, List<String>> getVernacularNamesByLanguage() {
		return summary.getLanguageToVernacularNames();
	}
	
	@ExternalizedProperty
	public Map<String, String> getInfoByName() {
		return summary.getInfoByName();
	}

	@ExternalizedProperty
	public TaxonRank getRank() {
		return summary.getRank();
	}

	@ExternalizedProperty
	public String getScientificName() {
		return summary.getScientificName();
	}

	@ExternalizedProperty
	public Integer getTaxonId() {
		return summary.getTaxonId();
	}

	@ExternalizedProperty
	public int getTaxonSystemId() {
		return summary.getTaxonSystemId();
	}

	@ExternalizedProperty
	public List<String> getVernacularLanguages() {
		return summary.getVernacularLanguages();
	}

}
