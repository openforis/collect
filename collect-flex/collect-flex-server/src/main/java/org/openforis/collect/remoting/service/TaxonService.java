/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
import org.openforis.collect.persistence.TaxonDAO;
import org.openforis.idm.model.TaxonOccurence;
import org.openforis.idm.model.species.Taxon;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 */
public class TaxonService {

	@Autowired
	private TaxonDAO taxonDAO;
	
	private enum SearchType {
		BY_CODE,
		BY_SCIENTIFIC_NAME
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

	private List<TaxonOccurrenceProxy> find(SearchType type, String searchString, int maxResults) {
		List<Taxon> list = null;
		switch(type) {
			case BY_CODE:
				list = taxonDAO.findByCode(searchString, maxResults);
				break;
			case BY_SCIENTIFIC_NAME:
				list = taxonDAO.findByScientificName(searchString, maxResults);
				break;
		}
		List<TaxonOccurrenceProxy> result = new ArrayList<TaxonOccurrenceProxy>();
		if(list != null) {
			for (Taxon taxon : list) {
				TaxonOccurence occurrence = new TaxonOccurence(taxon);
				TaxonOccurrenceProxy proxy = new TaxonOccurrenceProxy(occurrence);
				result.add(proxy);
			}
		}
		return result;	
	}
	
	public void suggest(TaxonOccurence taxon) {

	}

	public void suggest(long taxonId, String taxonName) {

	}

}
