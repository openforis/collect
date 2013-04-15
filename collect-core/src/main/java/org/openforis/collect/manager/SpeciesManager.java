/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.TaxonTree;
import org.openforis.collect.model.TaxonTree.Node;
import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonVernacularNameDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 * @author M. Togna
 * @author E. Wibowo
 * 
 */
public class SpeciesManager {
	
	private final Log log = LogFactory.getLog(SpeciesManager.class);
	
	@Autowired
	private TaxonDao taxonDao;
	@Autowired
	private TaxonVernacularNameDao taxonVernacularNameDao;
	@Autowired
	private TaxonomyDao taxonomyDao;
	@Autowired
	private ExpressionFactory expressionFactory;

	@Transactional
	public List<CollectTaxonomy> loadTaxonomiesBySurvey(int surveyId) {
		return taxonomyDao.loadAllBySurvey(surveyId);
	}
	
	@Transactional
	public List<CollectTaxonomy> loadTaxonomiesBySurveyWork(int surveyId) {
		return taxonomyDao.loadAllBySurveyWork(surveyId);
	}
	
	@Transactional
	public CollectTaxonomy loadTaxonomyById(int id) {
		return taxonomyDao.loadById(id);
	}

	@Transactional
	public CollectTaxonomy loadTaxonomyByName(int surveyId, String name) {
		return taxonomyDao.load(surveyId, name);
	}

	@Transactional
	public CollectTaxonomy loadTaxonomyWorkByName(int surveyId, String name) {
		return taxonomyDao.loadBySurveyWork(surveyId, name);
	}

	@Transactional
	public List<TaxonOccurrence> findByCode(int surveyId, String taxonomyName, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.load(surveyId, taxonomyName);
		return findByCode(taxonomy.getId(), searchString, maxResults);
	}

	@Transactional
	public List<TaxonOccurrence> findByCode(int taxonomyId, String searchString, int maxResults) {
		List<Taxon> list = taxonDao.findByCode(taxonomyId, searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon.getTaxonId(), taxon.getCode(), taxon.getScientificName());
			result.add(o);
		}
		return result;
	}
	
	@Transactional
	public List<TaxonOccurrence> findByScientificName(int surveyId, String taxonomyName, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.load(surveyId, taxonomyName);
		return findByScientificName(taxonomy.getId(), searchString, maxResults);
	}

	@Transactional
	public List<TaxonOccurrence> findByScientificName(int taxonomyId, String searchString, int maxResults) {
		List<Taxon> list = taxonDao.findByScientificName(taxonomyId, searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon.getTaxonId(), taxon.getCode(), taxon.getScientificName());
			result.add(o);
		}
		return result;
	}

	@Transactional
	public List<TaxonOccurrence> findByVernacularName(int surveyId, String taxonomyName, String searchString, int maxResults) {
		return findByVernacularName(surveyId, taxonomyName, (TaxonAttribute) null, searchString, maxResults);
	}
	
	@Transactional
	public List<TaxonOccurrence> findByVernacularName(int surveyId, String taxonomyName, TaxonAttribute attr, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.load(surveyId, taxonomyName);
		Integer taxonomyId = taxonomy.getId();
		return findByVernacularName(taxonomyId, attr, searchString, maxResults);
	}

	@Transactional
	public List<TaxonOccurrence> findByVernacularName(int taxonomyId, TaxonAttribute attr, String searchString, int maxResults) {
		List<TaxonVernacularName> list = null;
		String[] qualifierValues = null;
		if ( attr != null ) {
			qualifierValues = extractQualifierValues(attr);
		}
		if (qualifierValues == null){
			list = taxonVernacularNameDao.findByVernacularName(taxonomyId, searchString, maxResults);
		} else{
			list = taxonVernacularNameDao.findByVernacularName(taxonomyId, searchString, qualifierValues, maxResults);
		}
		List<TaxonOccurrence> result = createOccurrenceList(list);
		return result;
	}
	
	@Transactional
	public TaxonSummaries loadTaxonSummaries(int taxonomyId) {
		return loadTaxonSummaries(taxonomyId, 0, Integer.MAX_VALUE);
	}
	
	@Transactional
	public TaxonSummaries loadTaxonSummaries(int taxonomyId, int offset, int maxRecords) {
		int totalCount = taxonDao.countTaxons(taxonomyId);
		Set<String> vernacularNamesLanguageCodes = new HashSet<String>();
		List<TaxonSummary> items = new ArrayList<TaxonSummary>();
		if ( totalCount > 0 ) {
			List<Taxon> taxons = taxonDao.loadTaxons(taxonomyId, offset, maxRecords);
			for (Taxon taxon : taxons) {
				TaxonSummary summary = createSummary(taxon);
				List<String> itemVernLangCodes = summary.getVernacularLanguages();
				vernacularNamesLanguageCodes.addAll(itemVernLangCodes);
				items.add(summary);
			}
		}
		List<String> sortedVernacularNamesLanguageCodes = new ArrayList<String>(vernacularNamesLanguageCodes);
		Collections.sort(sortedVernacularNamesLanguageCodes);
		TaxonSummaries result = new TaxonSummaries(totalCount, items, sortedVernacularNamesLanguageCodes);
		return result;
	}

	protected TaxonSummary createSummary(Taxon taxon) {
		TaxonSummary summary = new TaxonSummary();
		summary.setCode(taxon.getCode());
		summary.setRank(taxon.getTaxonRank());
		summary.setScientificName(taxon.getScientificName());
		summary.setTaxonId(taxon.getTaxonId());
		List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(taxon.getSystemId());
		for (TaxonVernacularName taxonVernacularName : vernacularNames) {
			//if lang code is blank, vernacular name will be considered as synonym
			String languageCode = StringUtils.trimToEmpty(taxonVernacularName.getLanguageCode());
			summary.addVernacularName(languageCode, taxonVernacularName.getVernacularName());
		}
		return summary;
	}
	
	@Transactional
	public void save(CollectTaxonomy taxonomy) {
		if ( taxonomy.getSurveyId() == null && taxonomy.getSurveyWorkId() == null ) {
			throw new IllegalArgumentException("Cannot save taxonomy: surveyId or surveyWorkId must be defined");
		} else if ( taxonomy.getSurveyId() != null && taxonomy.getSurveyWorkId() != null ) {
			throw new IllegalArgumentException("Cannot save taxonomy: surveyId and surveyWorkId must not be both defined");
		}
		if ( taxonomy.getId() == null ) {
			taxonomyDao.insert(taxonomy);
		} else {
			taxonomyDao.update(taxonomy);
		}
	}
	
	@Transactional
	public void delete(CollectTaxonomy taxonomy) {
		Integer id = taxonomy.getId();
		deleteTaxonsByTaxonomy(taxonomy);
		taxonomyDao.delete(id);
	}

	@Transactional
	public void deleteTaxonsByTaxonomy(CollectTaxonomy taxonomy) {
		Integer id = taxonomy.getId();
		taxonVernacularNameDao.deleteByTaxonomy(id);
		taxonDao.deleteByTaxonomy(id);
	}
	
	@Transactional
	public void save(Taxon taxon) {
		if ( taxon.getSystemId() == null ) {
			taxonDao.insert(taxon);
		} else {
			taxonDao.update(taxon);
		}
	}

	@Transactional
	public void delete(Taxon taxon) {
		taxonDao.delete(taxon.getSystemId());
	}
	
	@Transactional
	public void save(TaxonVernacularName vernacularName) {
		if ( vernacularName.getId() == null ) {
			taxonVernacularNameDao.insert(vernacularName);
		} else {
			taxonVernacularNameDao.update(vernacularName);
		}
	}
	
	@Transactional
	public void delete(TaxonVernacularName vernacularName) {
		taxonVernacularNameDao.delete(vernacularName.getId());
	}
	
	@Transactional
	public void publishTaxonomies(Integer surveyWorkId, int publishedSurveyId) {
		deleteTaxonomies(publishedSurveyId);
		List<CollectTaxonomy> taxonomies = taxonomyDao.loadAllBySurveyWork(surveyWorkId);
		for (CollectTaxonomy taxonomy : taxonomies) {
			taxonomy.setSurveyWorkId(null);
			taxonomy.setSurveyId(publishedSurveyId);
			taxonomyDao.update(taxonomy);
		}
	}

	protected void deleteTaxonomies(int surveyId) {
		List<CollectTaxonomy> publishedTaxonomies = taxonomyDao.loadAllBySurvey(surveyId);
		for (CollectTaxonomy taxonomy : publishedTaxonomies) {
			delete(taxonomy);
		}
	}
	
	@Transactional
	public void duplicateTaxonomyForWork(int publishedSurveyId, Integer surveyWorkId) {
		List<CollectTaxonomy> taxonomies = taxonomyDao.loadAllBySurvey(publishedSurveyId);
		for (CollectTaxonomy taxonomy : taxonomies) {
			int oldTaxonomyId = taxonomy.getId();
			taxonomy.setId(null);
			taxonomy.setSurveyId(null);
			taxonomy.setSurveyWorkId(surveyWorkId);
			taxonomyDao.insert(taxonomy);
			Integer newTaxonomyId = taxonomy.getId();
			duplicateTaxons(oldTaxonomyId, newTaxonomyId);

		}
	}

	protected void duplicateTaxons(int oldTaxonomyId, Integer newTaxonomyId) {
		List<Taxon> taxons = taxonDao.loadTaxonsForTreeBuilding(oldTaxonomyId);
		Map<Integer, Taxon> oldIdToNewTaxon = new HashMap<Integer, Taxon>();
		for (Taxon taxon : taxons) {
			Integer oldId = taxon.getSystemId();
			Integer oldParentId = taxon.getParentId();
			if ( oldParentId != null ) {
				Taxon newParent = oldIdToNewTaxon.get(oldParentId);
				taxon.setParentId(newParent.getSystemId());
			}
			taxon.setSystemId(null);
			taxon.setTaxonomyId(newTaxonomyId);
			taxonDao.insert(taxon);
			int newTaxonId = taxon.getSystemId();
			duplicateVernacularNames(oldId, newTaxonId);
			oldIdToNewTaxon.put(oldId, taxon);
		}
	}

	protected void duplicateVernacularNames(int oldTaxonId, int newTaxonId) {
		List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(oldTaxonId);
		for (TaxonVernacularName vernacularName : vernacularNames) {
			vernacularName.setId(null);
			vernacularName.setTaxonSystemId(newTaxonId);
			taxonVernacularNameDao.insert(vernacularName);
		}
	}
	
	protected List<TaxonOccurrence> createOccurrenceList(
			List<TaxonVernacularName> vernacularNames) {
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (TaxonVernacularName vernacularName : vernacularNames) {
			Integer taxonId = vernacularName.getTaxonSystemId();
			Taxon taxon = taxonDao.loadById(taxonId);
			TaxonOccurrence o = new TaxonOccurrence(taxon, vernacularName);
			result.add(o);
		}
		return result;
	}

	protected String[] extractQualifierValues(TaxonAttribute attr) {
		TaxonAttributeDefinition defn = attr.getDefinition();
		List<String> qualifiers = defn.getQualifiers();
		String[] qualifierValues = null;
		if ( qualifiers != null && ! qualifiers.isEmpty() ) {
			qualifierValues = new String[qualifiers.size()];
			Entity parent = attr.getParent();
			for (int i = 0; i < qualifiers.size(); i++) {
				String qualifierExpr = qualifiers.get(i);
				try {
					ModelPathExpression expression = expressionFactory.createModelPathExpression(qualifierExpr);
					CodeAttribute code = (CodeAttribute) expression.evaluate(parent, null);
					String qualifierValue = code.getValue().getCode();
					qualifierValues[i] = qualifierValue;
				} catch (Exception e) {
					if ( log.isWarnEnabled() ) {
						log.warn("Error evaluating taxon qualifiers: ", e);
					}
					break;
				}
			}
		}
		return qualifierValues;
	}

	@Transactional
	public TaxonTree createTaxonTree(int taxonomyId) {
		List<Taxon> taxons = taxonDao.loadTaxonsForTreeBuilding(taxonomyId);
		TaxonTree tree = new TaxonTree();
		Map<Integer, Taxon> idToTaxon = new HashMap<Integer, Taxon>();
		for (Taxon taxon : taxons) {
			Integer systemId = taxon.getSystemId();
			Integer parentId = taxon.getParentId();
			Taxon parent = parentId == null ? null: idToTaxon.get(parentId);
			Node newNode = tree.addNode(parent, taxon);
			List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(systemId);
			newNode.setVernacularNames(vernacularNames);
			idToTaxon.put(systemId, taxon);
		}
		return tree;
	}

	public TaxonDao getTaxonDao() {
		return taxonDao;
	}

	public void setTaxonDao(TaxonDao taxonDao) {
		this.taxonDao = taxonDao;
	}

	public TaxonVernacularNameDao getTaxonVernacularNameDao() {
		return taxonVernacularNameDao;
	}

	public void setTaxonVernacularNameDao(
			TaxonVernacularNameDao taxonVernacularNameDao) {
		this.taxonVernacularNameDao = taxonVernacularNameDao;
	}

	public TaxonomyDao getTaxonomyDao() {
		return taxonomyDao;
	}

	public void setTaxonomyDao(TaxonomyDao taxonomyDao) {
		this.taxonomyDao = taxonomyDao;
	}

	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}

	public void setExpressionFactory(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
	}
	
}
