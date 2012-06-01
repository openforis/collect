/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.model.TaxonOccurrence;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * @author S. Ricci
 * @author E. Wibowo 
 */
public class SpeciesService {

	@Autowired
	private SpeciesManager taxonManager;
	
	@Autowired
	private SessionManager sessionManager;
	
	public List<TaxonOccurrenceProxy> findByCode(String taxonomy, String searchString, int maxResults) {
		List<TaxonOccurrence> list = taxonManager.findByCode(taxonomy, searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}
	
	public List<TaxonOccurrenceProxy> findByScientificName(String taxonomy, String searchString, int maxResults) {
		List<TaxonOccurrence> list = taxonManager.findByScientificName(taxonomy, searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}
	
	public List<TaxonOccurrenceProxy> findByVernacularName(String taxonomy, int nodeId, String searchString, int maxResults) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord activeRecord = sessionState.getActiveRecord();
		List<TaxonOccurrence> list = taxonManager.findByVernacularName(taxonomy, activeRecord, nodeId, searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}
	
}
