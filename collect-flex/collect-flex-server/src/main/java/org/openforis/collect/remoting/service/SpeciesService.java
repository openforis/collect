/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
import org.openforis.idm.model.TaxonOccurrence;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class SpeciesService {

	@Autowired
	private SpeciesManager taxonManager;
	
	/**
	 * @param searchString 
	 * @param maxResults
	 */
	public List<TaxonOccurrenceProxy> findByCode(String taxonomy, String searchString, int maxResults) {
		List<TaxonOccurrence> list = taxonManager.findByCode(taxonomy, searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}

	/**
	 * 
	 * @param searchString
	 * @param maxResults
	 * @return
	 */
	public List<TaxonOccurrenceProxy> findByScientificName(String taxonomy, String searchString, int maxResults) {
		List<TaxonOccurrence> list = taxonManager.findByScientificName(taxonomy, searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}
	
	/**
	 * 
	 * @param searchString
	 * @param maxResults
	 * @return
	 */
	public List<TaxonOccurrenceProxy> findByVernacularName(String taxonomy, String searchString, int maxResults) {
		List<TaxonOccurrence> list = taxonManager.findByVernacularName(taxonomy, searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}

}
