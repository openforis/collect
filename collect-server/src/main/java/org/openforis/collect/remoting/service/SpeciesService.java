/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
import org.openforis.collect.model.proxy.TaxonomyProxy;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

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
	
	@Secured("ROLE_ENTRY")
	public List<TaxonomyProxy> loadAllTaxonomies() {
		List<Taxonomy> result = taxonManager.loadAllTaxonomies();
		List<TaxonomyProxy> proxies = TaxonomyProxy.fromList(result);
		return proxies;
	}
	
	@Secured("ROLE_ADMIN")
	public TaxonomyProxy saveTaxonomy(TaxonomyProxy proxy) {
		Integer taxonomyId = proxy.getId();
		Taxonomy taxonomy;
		if ( taxonomyId == null ) {
			taxonomy = new Taxonomy();
		} else {
			taxonomy = taxonManager.loadTaxonomyById(taxonomyId);
		}
		proxy.copyTo(taxonomy);
		taxonManager.save(taxonomy);
		TaxonomyProxy result = new TaxonomyProxy(taxonomy);
		return result;
	}

	@Secured("ROLE_ADMIN")
	public void deleteTaxonomy(TaxonomyProxy proxy) {
		Integer taxonomyId = proxy.getId();
		Taxonomy taxonomy = taxonManager.loadTaxonomyById(taxonomyId);
		taxonManager.delete(taxonomy);
	}
	
	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByCode(String taxonomy, String searchString, int maxResults) {
		List<TaxonOccurrence> list = taxonManager.findByCode(taxonomy, searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}
	
	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByScientificName(String taxonomy, String searchString, int maxResults) {
		List<TaxonOccurrence> list = taxonManager.findByScientificName(taxonomy, searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}
	
	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByVernacularName(String taxonomy, int nodeId, String searchString, int maxResults) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord activeRecord = sessionState.getActiveRecord();
		Node<? extends NodeDefinition> attr = activeRecord.getNodeByInternalId(nodeId);
		if ( attr instanceof TaxonAttribute ) {
			List<TaxonOccurrence> list = taxonManager.findByVernacularName(taxonomy, (TaxonAttribute) attr, searchString, maxResults);
			List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
			return result;
		} else {
			throw new IllegalArgumentException("TaxonAttribute expected");
		}
	}
	
}
