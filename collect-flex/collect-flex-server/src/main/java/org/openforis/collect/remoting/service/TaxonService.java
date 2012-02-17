/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.TaxonManager;
import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
import org.openforis.idm.model.TaxonOccurrence;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class TaxonService {

	@Autowired
	private TaxonManager taxonManager;
	
	private enum SearchType {
		BY_CODE,
		BY_SCIENTIFIC_NAME,
		BY_VERNACULAR_NAME
	}
	
	/**
	 * @param searchString 
	 * @param maxResults 
	 */
	public List<TaxonOccurrenceProxy> findByCode(String searchString, int maxResults) {
		return find(SearchType.BY_CODE, searchString, maxResults);
	}

	/**
	 * 
	 * @param searchString
	 * @param maxResults
	 * @return
	 */
	public List<TaxonOccurrenceProxy> findByScientificName(String searchString, int maxResults) {
		return find(SearchType.BY_SCIENTIFIC_NAME, searchString, maxResults);
	}
	
	/**
	 * 
	 * @param searchString
	 * @param maxResults
	 * @return
	 */
	public List<TaxonOccurrenceProxy> findByVernacularName(String searchString, int maxResults) {
		return find(SearchType.BY_VERNACULAR_NAME, searchString, maxResults);
	}

	/**
	 * Generic find method that uses SearchType enum
	 * 
	 * @param type
	 * @param searchString
	 * @param maxResults
	 * @return
	 */
	private List<TaxonOccurrenceProxy> find(SearchType type, String searchString, int maxResults) {
		List<TaxonOccurrence> list = null;
		switch(type) {
			case BY_CODE:
				list = taxonManager.findByCode(searchString, maxResults);
				break;
			case BY_SCIENTIFIC_NAME:
				list = taxonManager.findByScientificName(searchString, maxResults);
				break;
			case BY_VERNACULAR_NAME:
				list = taxonManager.findByVernacularName(searchString, maxResults);
				break;
		}
		List<TaxonOccurrenceProxy> result = new ArrayList<TaxonOccurrenceProxy>();
		if(list != null) {
			for (TaxonOccurrence o : list) {
				TaxonOccurrenceProxy proxy = new TaxonOccurrenceProxy(o);
				result.add(proxy);
			}
		}
		return result;	
	}
	
	public void suggest(TaxonOccurrence taxon) {

	}

	public void suggest(long taxonId, String taxonName) {

	}

}
