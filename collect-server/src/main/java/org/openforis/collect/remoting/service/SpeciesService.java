/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.proxy.TaxonSummariesProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
import org.openforis.collect.model.proxy.TaxonomyProxy;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TaxonOccurrence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * @author M. Togna
 * @author S. Ricci
 * @author E. Wibowo 
 */
public class SpeciesService {

	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private SessionManager sessionManager;
	
	@Secured("ROLE_ENTRY")
	public List<TaxonomyProxy> loadTaxonomiesBySurvey(int surveyId, boolean work) {
		List<CollectTaxonomy> result;
		if ( work ) {
			result = speciesManager.loadTaxonomiesBySurveyWork(surveyId);
		} else {
			result = speciesManager.loadTaxonomiesBySurvey(surveyId);
		}
		List<TaxonomyProxy> proxies = TaxonomyProxy.fromList(result);
		return proxies;
	}
	
	@Secured("ROLE_ADMIN")
	public TaxonSummariesProxy loadTaxonSummaries(int taxonomyId, int offset, int maxRecords) {
		TaxonSummaries summaries = speciesManager.loadTaxonSummaries(taxonomyId, offset, maxRecords);
		return new TaxonSummariesProxy(summaries);
	}
	
	@Secured("ROLE_ADMIN")
	public TaxonomyProxy saveTaxonomy(TaxonomyProxy proxy) {
		CollectTaxonomy taxonomy;
		Integer taxonomyId = proxy.getId();
		if ( taxonomyId == null ) {
			taxonomy = new CollectTaxonomy();
		} else {
			taxonomy = speciesManager.loadTaxonomyById(taxonomyId);
		}
		String oldName = taxonomy.getName();
		String newName = proxy.getName();
		if ( oldName != null && ! oldName.equals(newName) ) {
			updateTaxonAttributeDefinitions(oldName, newName);
		}
		proxy.copyPropertiesForUpdate(taxonomy);
		speciesManager.save(taxonomy);
		TaxonomyProxy result = new TaxonomyProxy(taxonomy);
		return result;
	}

	protected void updateTaxonAttributeDefinitions(String oldName, String newName) {
		List<TaxonAttributeDefinition> defns = getTaxonAttributeDefinitionsForDesignerSurvey(oldName);
		if ( ! defns.isEmpty() ) {
			for (TaxonAttributeDefinition defn : defns) {
				if ( defn.getTaxonomy().equals(oldName) ) {
					defn.setTaxonomy(newName);
				}
			}
			sessionManager.saveActiveDesignerSurvey();
		}
	}

	@Secured("ROLE_ADMIN")
	public boolean isTaxonomyInUse(String taxonomyName) {
		CollectSurvey survey = sessionManager.getActiveDesignerSurvey();
		Schema schema = survey.getSchema();
		List<TaxonAttributeDefinition> defns = schema.getTaxonAttributeDefinitions(taxonomyName);
		return ! defns.isEmpty();
	}
	
	@Secured("ROLE_ADMIN")
	public void deleteTaxonomy(TaxonomyProxy proxy) {
		Integer taxonomyId = proxy.getId();
		CollectTaxonomy taxonomy = speciesManager.loadTaxonomyById(taxonomyId);
		speciesManager.delete(taxonomy);
		deleteReferencingAttributes(taxonomy);
	}

	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByCode(String taxonomyName, String searchString, int maxResults) {
		CollectTaxonomy taxonomy = getTaxonomyByActiveSurvey(taxonomyName);
		List<TaxonOccurrence> list = speciesManager.findByCode(taxonomy.getId(), searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}

	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByScientificName(String taxonomyName, String searchString, int maxResults) {
		CollectTaxonomy taxonomy = getTaxonomyByActiveSurvey(taxonomyName);
		List<TaxonOccurrence> list = speciesManager.findByScientificName(taxonomy.getId(), searchString, maxResults);
		List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
		return result;
	}

	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByVernacularName(String taxonomyName, int nodeId, String searchString, int maxResults) {
		CollectTaxonomy taxonomy = getTaxonomyByActiveSurvey(taxonomyName);
		CollectRecord activeRecord = sessionManager.getActiveRecord();
		Node<? extends NodeDefinition> attr = activeRecord.getNodeByInternalId(nodeId);
		if ( attr instanceof TaxonAttribute ) {
			List<TaxonOccurrence> list = speciesManager.findByVernacularName(taxonomy.getId(), (TaxonAttribute) attr, searchString, maxResults);
			List<TaxonOccurrenceProxy> result = TaxonOccurrenceProxy.fromList(list);
			return result;
		} else {
			throw new IllegalArgumentException("TaxonAttribute expected");
		}
	}
	
	protected CollectTaxonomy getTaxonomyByActiveSurvey(String taxonomyName) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		boolean activeSurveyWork = sessionState.isActiveSurveyWork();
		Integer surveyId = activeSurvey.getId();
		CollectTaxonomy taxonomy;
		if ( activeSurveyWork ) {
			taxonomy = speciesManager.loadTaxonomyWorkByName(surveyId, taxonomyName);
		} else {
			taxonomy = speciesManager.loadTaxonomyByName(surveyId, taxonomyName);
		}
		return taxonomy;
	}

	protected List<TaxonAttributeDefinition> getTaxonAttributeDefinitionsForDesignerSurvey(String oldName) {
		CollectSurvey survey = sessionManager.getActiveDesignerSurvey();
		Schema schema = survey.getSchema();
		List<TaxonAttributeDefinition> defns = schema.getTaxonAttributeDefinitions(oldName);
		return defns;
	}

	protected void deleteReferencingAttributes(CollectTaxonomy taxonomy) {
		List<TaxonAttributeDefinition> defns = getTaxonAttributeDefinitionsForDesignerSurvey(taxonomy.getName());
		if ( ! defns.isEmpty() ) {
			for (TaxonAttributeDefinition defn : defns) {
				EntityDefinition parent = (EntityDefinition) defn.getParentDefinition();
				parent.removeChildDefinition(defn);
			}
			sessionManager.saveActiveDesignerSurvey();
		}
	}
	
}
