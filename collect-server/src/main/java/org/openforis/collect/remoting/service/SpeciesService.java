/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.List;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.TaxonSearchParameters;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.proxy.TaxonSummariesProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
import org.openforis.collect.model.proxy.TaxonomyProxy;
import org.openforis.collect.utils.Proxies;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TaxonOccurrence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;

/**
 * @author M. Togna
 * @author S. Ricci
 * @author E. Wibowo 
 */
public class SpeciesService {

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	@Qualifier("sessionManager")
	private SessionManager sessionManager;

	
	@Secured("ROLE_ENTRY")
	public List<TaxonomyProxy> loadTaxonomiesBySurvey(int surveyId, boolean work) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		List<CollectTaxonomy> result = speciesManager.loadTaxonomiesBySurvey(survey);
		return Proxies.fromList(result, TaxonomyProxy.class);
	}
	
	@Secured("ROLE_ADMIN")
	public TaxonSummariesProxy loadTaxonSummaries(int taxonomyId, int offset, int maxRecords) {
		CollectSurvey survey = getActiveSurvey();
		TaxonSummaries summaries = speciesManager.loadTaxonSummaries(survey, taxonomyId, offset, maxRecords);
		return new TaxonSummariesProxy(summaries);
	}
	
	@Secured("ROLE_ADMIN")
	public TaxonomyProxy saveTaxonomy(TaxonomyProxy proxy) {
		CollectSurvey survey = sessionManager.getActiveDesignerSurvey();
		CollectTaxonomy taxonomy;
		Integer taxonomyId = proxy.getId();
		if ( taxonomyId == null ) {
			taxonomy = new CollectTaxonomy();
		} else {
			taxonomy = speciesManager.loadTaxonomyById(survey, taxonomyId);
		}
		String oldName = taxonomy.getName();
		String newName = proxy.getName();
		if ( oldName != null && ! oldName.equals(newName) ) {
			updateTaxonAttributeDefinitions(oldName, newName);
		}
		proxy.copyPropertiesForUpdate(taxonomy);
		speciesManager.save(taxonomy);
		return new TaxonomyProxy(taxonomy);
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
		CollectSurvey survey = sessionManager.getActiveDesignerSurvey();
		Integer taxonomyId = proxy.getId();
		CollectTaxonomy taxonomy = speciesManager.loadTaxonomyById(survey, taxonomyId);
		speciesManager.delete(taxonomy);
		deleteReferencingAttributes(taxonomy);
	}

	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByFamilyCode(String taxonomyName, String searchString, int maxResults, TaxonSearchParameters parameters) {
		CollectTaxonomy taxonomy = loadTaxonomyByActiveSurvey(taxonomyName);
		List<TaxonOccurrence> list = speciesManager.findByFamilyCode(taxonomy, searchString, maxResults, parameters);
		return Proxies.fromList(list, TaxonOccurrenceProxy.class);
	}
	
	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByFamilyScientificName(String taxonomyName, String searchString, int maxResults, 
			TaxonSearchParameters parameters) {
		CollectTaxonomy taxonomy = loadTaxonomyByActiveSurvey(taxonomyName);
		List<TaxonOccurrence> list = speciesManager.findByFamilyScientificName(taxonomy, searchString, maxResults, parameters);
		return Proxies.fromList(list, TaxonOccurrenceProxy.class);
	}

	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByCode(String taxonomyName, String searchString, int maxResults, TaxonSearchParameters parameters) {
		CollectTaxonomy taxonomy = loadTaxonomyByActiveSurvey(taxonomyName);
		List<TaxonOccurrence> list = speciesManager.findByCode(taxonomy, searchString, maxResults, parameters);
		return Proxies.fromList(list, TaxonOccurrenceProxy.class);
	}

	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByScientificName(String taxonomyName, String searchString, int maxResults, 
			TaxonSearchParameters parameters) {
		CollectTaxonomy taxonomy = loadTaxonomyByActiveSurvey(taxonomyName);
		List<TaxonOccurrence> list = speciesManager.findByScientificName(taxonomy, searchString, maxResults, parameters);
		return Proxies.fromList(list, TaxonOccurrenceProxy.class);
	}

	@Secured("ROLE_ENTRY")
	public List<TaxonOccurrenceProxy> findByVernacularName(String taxonomyName, int nodeId, String searchString, int maxResults, 
			TaxonSearchParameters parameters) {
		CollectTaxonomy taxonomy = loadTaxonomyByActiveSurvey(taxonomyName);
		CollectRecord activeRecord = sessionManager.getActiveRecord();
		Node<? extends NodeDefinition> attr = activeRecord.getNodeByInternalId(nodeId);
		if ( attr instanceof TaxonAttribute ) {
			List<TaxonOccurrence> list = speciesManager.findByVernacularName(taxonomy, (TaxonAttribute) attr, searchString, 
					maxResults, parameters);
			return Proxies.fromList(list, TaxonOccurrenceProxy.class);
		} else {
			throw new IllegalArgumentException("TaxonAttribute expected, found: " + attr.getClass().getName());
		}
	}
	
	private CollectSurvey getActiveSurvey() {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		return activeSurvey;
	}

	protected CollectTaxonomy loadTaxonomyByActiveSurvey(String taxonomyName) {
		CollectSurvey activeSurvey = getActiveSurvey();
		return loadTaxonomy(activeSurvey, taxonomyName);
	}

	private CollectTaxonomy loadTaxonomy(CollectSurvey survey, String name) {
		return speciesManager.loadTaxonomyByName(survey, name);
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
