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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.TaxonTree;
import org.openforis.collect.model.TaxonTree.Node;
import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonVernacularNameDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 * @author M. Togna
 * @author E. Wibowo
 * 
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class SpeciesManager {
	
	private final Log log = LogFactory.getLog(SpeciesManager.class);
	
	private static final int CONFIRMED_TAXON_STEP_NUMBER = 9;

	private static final int TAXON_INSERT_BUFFER_SIZE = 1000;
	private static final int TAXON_VERNACULAR_NAME_INSERT_BUFFER_SIZE = 1000;
	
	@Autowired
	private TaxonDao taxonDao;
	@Autowired
	private TaxonVernacularNameDao taxonVernacularNameDao;
	@Autowired
	private TaxonomyDao taxonomyDao;
	@Autowired
	private ExpressionFactory expressionFactory;
	
	private transient Map<Integer, TaxonTree> taxonTreeByTaxonomyIdCache = new HashMap<Integer, TaxonTree>();

	public List<CollectTaxonomy> loadTaxonomiesBySurvey(int surveyId) {
		return taxonomyDao.loadAllBySurvey(surveyId);
	}
	
	public CollectTaxonomy loadTaxonomyById(int id) {
		return taxonomyDao.loadById(id);
	}

	public CollectTaxonomy loadTaxonomyByName(int surveyId, String name) {
		return taxonomyDao.loadByName(surveyId, name);
	}

	public List<TaxonOccurrence> findByFamilyCode(CollectSurvey survey, int taxonomyId, String searchString, int maxResults, 
			TaxonSearchParameters parameters) {
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		TaxonTree taxonTree = loadTaxonTree(survey, taxonomyId);
		List<Taxon> families = taxonTree.findTaxaByCodeStartingWith(searchString, TaxonRank.FAMILY);
		result.addAll(fromTaxonomiesToTaxonOccurrences(families, parameters));
		for (Taxon familyTaxon : families) {
			List<TaxonOccurrence> descendants = taxonTree.getDescendantOccurrences(familyTaxon, parameters);
			result.addAll(descendants);
		}
		return result;
	}

	public List<TaxonOccurrence> findByCode(int surveyId, String taxonomyName, String searchString, int maxResults) {
		return findByCode(surveyId, taxonomyName, searchString, maxResults, new TaxonSearchParameters());
	}
	
	public List<TaxonOccurrence> findByCode(int surveyId, String taxonomyName, String searchString, int maxResults, 
			TaxonSearchParameters parameters) {
		Taxonomy taxonomy = taxonomyDao.loadByName(surveyId, taxonomyName);
		return findByCode(taxonomy.getId(), searchString, maxResults, parameters);
	}

	public List<TaxonOccurrence> findByCode(int taxonomyId, String searchString, int maxResults) {
		return findByCode(taxonomyId, searchString, maxResults, new TaxonSearchParameters());
	}
	
	public List<TaxonOccurrence> findByCode(int taxonomyId, TaxonRank rank, String searchString, int maxResults, TaxonSearchParameters parameters) {
		List<Taxon> list = taxonDao.findByCode(taxonomyId, searchString, maxResults);
		return fromTaxonomiesToTaxonOccurrences(list, parameters);
	}

	public List<TaxonOccurrence> findByCode(int taxonomyId, String searchString, int maxResults, TaxonSearchParameters parameters) {
		List<Taxon> list = taxonDao.findByCode(taxonomyId, searchString, maxResults);
		return fromTaxonomiesToTaxonOccurrences(list, parameters);
	}

	public List<TaxonOccurrence> findByScientificName(int surveyId, String taxonomyName, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.loadByName(surveyId, taxonomyName);
		return findByScientificName(taxonomy.getId(), searchString, maxResults);
	}

	public List<TaxonOccurrence> findByScientificName(int taxonomyId, String searchString, int maxResults) {
		return findByScientificName(taxonomyId, searchString, maxResults, new TaxonSearchParameters());
	}
	
	public List<TaxonOccurrence> findByScientificName(int taxonomyId, String searchString, int maxResults, TaxonSearchParameters parameters) {
		List<Taxon> list = taxonDao.findByScientificName(taxonomyId, searchString, maxResults);
		return fromTaxonomiesToTaxonOccurrences(list, parameters);
	}

	public List<TaxonOccurrence> findByFamilyScientificName(CollectSurvey survey, int taxonomyId, String searchString, int maxResults, 
			TaxonSearchParameters parameters) {
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		TaxonTree taxonTree = loadTaxonTree(survey, taxonomyId);
		List<Taxon> families = taxonTree.findTaxaByScientificNameStartingWith(searchString, TaxonRank.FAMILY);
		result.addAll(fromTaxonomiesToTaxonOccurrences(families, parameters));
		for (Taxon familyTaxon : families) {
			List<TaxonOccurrence> descendants = taxonTree.getDescendantOccurrences(familyTaxon, parameters);
			result.addAll(descendants);
		}
		return result;
	}
	
	private List<TaxonOccurrence> fromTaxonomiesToTaxonOccurrences(List<Taxon> list, TaxonSearchParameters parameters) {
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>(list.size());
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon);
			o.setTaxonRank(taxon.getTaxonRank());
			if (parameters.isIncludeUniqueVernacularName()) {
				includeUniqueVernacularNameIfAny(taxon.getSystemId(), o);
			}
			if (parameters.isIncludeAncestorTaxons()) {
				loadAncestorTaxons(taxon, o);
			}
			result.add(o);
		}
		return result;
	}
	
	private List<TaxonOccurrence> fromVernacularNamesToTaxonOccurrences(
			List<TaxonVernacularName> vernacularNames, TaxonSearchParameters parameters) {
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (TaxonVernacularName vernacularName : vernacularNames) {
			Integer taxonId = vernacularName.getTaxonSystemId();
			Taxon taxon = taxonDao.loadById(taxonId);
			TaxonOccurrence o = new TaxonOccurrence(taxon, vernacularName);
			if (parameters.isIncludeAncestorTaxons()) {
				loadAncestorTaxons(taxon, o);
			}
			result.add(o);
		}
		return result;
	}

	private void includeUniqueVernacularNameIfAny(int taxonSystemId, TaxonOccurrence o) {
		List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(taxonSystemId);
		if (vernacularNames.size() == 1) {
			TaxonVernacularName vernacularName = vernacularNames.get(0);
			o.setVernacularName(vernacularName.getVernacularName());
			o.setLanguageCode(vernacularName.getLanguageCode());
			o.setLanguageVariety(vernacularName.getLanguageVariety());
		}
	}

	private void loadAncestorTaxons(Taxon taxon, TaxonOccurrence o) {
		Taxon currentTaxon = taxon;
		while (currentTaxon.getParentId() != null) {
			Taxon parentTaxon = taxonDao.loadById(currentTaxon.getParentId());
			TaxonOccurrence parentTaxonOccurrence = new TaxonOccurrence(parentTaxon);
			o.addAncestorTaxon(parentTaxonOccurrence);
			currentTaxon = parentTaxon;
		}
	}

	public List<TaxonOccurrence> findByVernacularName(int surveyId, String taxonomyName, String searchString, int maxResults) {
		return findByVernacularName(surveyId, taxonomyName, (TaxonAttribute) null, searchString, maxResults);
	}
	
	public List<TaxonOccurrence> findByVernacularName(int surveyId, String taxonomyName, TaxonAttribute attr, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.loadByName(surveyId, taxonomyName);
		Integer taxonomyId = taxonomy.getId();
		return findByVernacularName(taxonomyId, attr, searchString, maxResults, new TaxonSearchParameters());
	}

	public List<TaxonOccurrence> findByVernacularName(int taxonomyId, TaxonAttribute attr, String searchString, int maxResults, 
			TaxonSearchParameters parameters) {
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
		List<TaxonOccurrence> result = fromVernacularNamesToTaxonOccurrences(list, parameters);
		return result;
	}
	
	public TaxonSummaries loadFullTaxonSummariesOld(CollectSurvey survey, int taxonomyId) {
		CollectTaxonomy taxonomy = loadTaxonomyById(taxonomyId);
		TaxonTree tree = loadTaxonTree(survey, taxonomyId);
		List<TaxonSummary> summaries = tree.toSummaries(TaxonRank.GENUS, false);
		List<String> sortedVernacularNamesLanguageCodes = new ArrayList<String>(tree.getVernacularLanguageCodes());
		Collections.sort(sortedVernacularNamesLanguageCodes);
		List<String> infoAttributeNames = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomy.getName()).getAttributeNames();
		return new TaxonSummaries(summaries.size(), summaries, sortedVernacularNamesLanguageCodes, infoAttributeNames);
	}
	
	public TaxonSummaries loadFullTaxonSummaries(CollectSurvey survey, int taxonomyId) {
		CollectTaxonomy taxonomy = loadTaxonomyById(taxonomyId);
		TaxonTree tree = loadTaxonTree(survey, taxonomyId);
		List<TaxonSummary> summaries = tree.toSummaries(TaxonRank.FAMILY, true);
		List<String> sortedVernacularNamesLanguageCodes = new ArrayList<String>(tree.getVernacularLanguageCodes());
		Collections.sort(sortedVernacularNamesLanguageCodes);
		List<String> infoAttributeNames = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomy.getName()).getAttributeNames();
		return new TaxonSummaries(summaries.size(), summaries, sortedVernacularNamesLanguageCodes, infoAttributeNames);
	}

	public TaxonSummaries loadTaxonSummaries(CollectSurvey survey, int taxonomyId) {
		return loadTaxonSummaries(survey, taxonomyId, 0, Integer.MAX_VALUE);
	}
	
	public TaxonSummaries loadTaxonSummaries(CollectSurvey survey, int taxonomyId, int offset, int maxRecords) {
		CollectTaxonomy taxonomy = loadTaxonomyById(taxonomyId);
		String taxonomyName = taxonomy.getName();
		int totalCount = taxonDao.countTaxons(taxonomyId);
		Set<String> vernacularNamesLanguageCodes = new HashSet<String>();
		List<TaxonSummary> items = new ArrayList<TaxonSummary>();
		TaxonomyDefinition taxonDefinition = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName);
		if ( totalCount > 0 ) {
			List<Taxon> taxons = taxonDao.loadTaxons(taxonomyId, offset, maxRecords);
			for (Taxon taxon : taxons) {
				List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(taxon.getSystemId());
				TaxonSummary summary = new TaxonSummary(taxonDefinition, taxon, vernacularNames, null);
				List<String> itemVernLangCodes = summary.getVernacularLanguages();
				vernacularNamesLanguageCodes.addAll(itemVernLangCodes);
				items.add(summary);
			}
		}
		List<String> sortedVernacularNamesLanguageCodes = new ArrayList<String>(vernacularNamesLanguageCodes);
		Collections.sort(sortedVernacularNamesLanguageCodes);
		List<String> infoAttributeNames = taxonDefinition.getAttributeNames();
		return new TaxonSummaries(totalCount, items, sortedVernacularNamesLanguageCodes, infoAttributeNames);
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void save(CollectTaxonomy taxonomy) {
		if ( taxonomy.getId() == null ) {
			taxonomyDao.insert(taxonomy);
		} else {
			taxonomyDao.update(taxonomy);
		}
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void delete(CollectTaxonomy taxonomy) {
		Integer id = taxonomy.getId();
		deleteTaxonsByTaxonomy(id);
		taxonomyDao.delete(id);
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void deleteTaxonsByTaxonomy(int id) {
		taxonVernacularNameDao.deleteByTaxonomy(id);
		taxonDao.deleteByTaxonomy(id);
		taxonTreeByTaxonomyIdCache.remove(id);
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void save(Taxon taxon) {
		if ( taxon.getSystemId() == null ) {
			taxonDao.insert(taxon);
		} else {
			taxonDao.update(taxon);
		}
		taxonTreeByTaxonomyIdCache.remove(taxon.getTaxonomyId());
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void insertVernacularNames(List<TaxonVernacularName> vernacularNames) {
		taxonVernacularNameDao.insert(vernacularNames);
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void insertTaxons(final int taxonomyId, TaxonTree tree, boolean overwriteAll) {
		CollectTaxonomy taxonomy = taxonomyDao.loadById(taxonomyId);
		if ( taxonomy == null ) {
			throw new IllegalStateException("Taxonomy not found");
		}
		if ( overwriteAll ) {
			deleteTaxonsByTaxonomy(taxonomyId);
		}
		final List<Taxon> taxonInsertBuffer = new ArrayList<Taxon>();
		final List<TaxonVernacularName> taxonVernacularNameInsertBuffer = new ArrayList<TaxonVernacularName>();
		int nextTaxonId = taxonDao.nextId();
		int nextTaxonVernacularNameId = taxonVernacularNameDao.nextId();
		tree.assignSystemIds(nextTaxonId, nextTaxonVernacularNameId);
		tree.depthFirstVisit(new TaxonTree.NodeVisitor() {
			@Override
			public void visit(Node node) {
				persistTaxonTreeNode(taxonInsertBuffer, taxonVernacularNameInsertBuffer, taxonomyId, node);
			}
		});
		flushTaxonInsertBuffer(taxonInsertBuffer);
		flushTaxonVernacularNameInsertBuffer(taxonVernacularNameInsertBuffer);
		
		taxonTreeByTaxonomyIdCache.remove(taxonomy.getId());
	}
	
	protected void persistTaxonTreeNode(List<Taxon> taxonInsertBuffer, List<TaxonVernacularName> taxonVernacularNameInsertBuffer, 
			int taxonomyId, Node node) {
		Taxon taxon = node.getTaxon();
		taxon.setTaxonomyId(taxonomyId);
		taxon.setStep(CONFIRMED_TAXON_STEP_NUMBER);
		taxonInsertBuffer.add(taxon);
		if ( taxonInsertBuffer.size() > TAXON_INSERT_BUFFER_SIZE ) {
			flushTaxonInsertBuffer(taxonInsertBuffer);
		}
		List<TaxonVernacularName> vernacularNames = node.getVernacularNames();
		for (TaxonVernacularName vernacularName : vernacularNames) {
			taxonVernacularNameInsertBuffer.add(vernacularName);
			if ( taxonInsertBuffer.size() > TAXON_VERNACULAR_NAME_INSERT_BUFFER_SIZE ) {
				flushTaxonVernacularNameInsertBuffer(taxonVernacularNameInsertBuffer);
			}	
		}
	}

	protected void flushTaxonInsertBuffer(List<Taxon> taxonInsertBuffer) {
		taxonDao.insert(taxonInsertBuffer);
		taxonInsertBuffer.clear();
	}

	protected void flushTaxonVernacularNameInsertBuffer(
			List<TaxonVernacularName> taxonVernacularNameInsertBuffer) {
		taxonVernacularNameDao.insert(taxonVernacularNameInsertBuffer);
		taxonVernacularNameInsertBuffer.clear();
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
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
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void delete(TaxonVernacularName vernacularName) {
		taxonVernacularNameDao.delete(vernacularName.getId());
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void moveTaxonomies(Integer fromSurveyId, int toSurveyId) {
		deleteTaxonomiesBySurvey(toSurveyId);
		List<CollectTaxonomy> taxonomies = taxonomyDao.loadAllBySurvey(fromSurveyId);
		for (CollectTaxonomy taxonomy : taxonomies) {
			taxonomy.setSurveyId(toSurveyId);
			taxonomyDao.update(taxonomy);
		}
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void deleteTaxonomiesBySurvey(int surveyId) {
		List<CollectTaxonomy> taxonomies = taxonomyDao.loadAllBySurvey(surveyId);
		for (CollectTaxonomy taxonomy : taxonomies) {
			delete(taxonomy);
		}
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void copyTaxonomy(int fromSurveyId, int toSurveyId) {
		List<CollectTaxonomy> taxonomies;
		taxonomies = taxonomyDao.loadAllBySurvey(fromSurveyId);
		for (CollectTaxonomy taxonomy : taxonomies) {
			int oldTaxonomyId = taxonomy.getId();
			taxonomy.setId(null);
			taxonomy.setSurveyId(toSurveyId);
			taxonomyDao.insert(taxonomy);
			Integer newTaxonomyId = taxonomy.getId();
			copyTaxons(oldTaxonomyId, newTaxonomyId);
		}
	}

	protected void copyTaxons(int oldTaxonomyId, Integer newTaxonomyId) {
		int taxonIdShift = taxonDao.duplicateTaxons(oldTaxonomyId, newTaxonomyId);
		taxonVernacularNameDao.duplicateVernacularNames(oldTaxonomyId, taxonIdShift);
		/*
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
		*/
	}
/*
	protected void duplicateVernacularNames(int oldTaxonId, int newTaxonId) {
		List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(oldTaxonId);
		for (TaxonVernacularName vernacularName : vernacularNames) {
			vernacularName.setId(null);
			vernacularName.setTaxonSystemId(newTaxonId);
			taxonVernacularNameDao.insert(vernacularName);
		}
	}
	*/
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

	public TaxonTree loadTaxonTree(CollectSurvey survey, int taxonomyId) {
		TaxonTree tree = taxonTreeByTaxonomyIdCache.get(taxonomyId);
		if (tree == null) {
			CollectTaxonomy taxonomy = loadTaxonomyById(taxonomyId);
			String taxonomyName = taxonomy.getName();
			TaxonomyDefinition taxonDefinition = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName);
			List<Taxon> taxons = taxonDao.loadTaxonsForTreeBuilding(taxonomyId);
			tree = new TaxonTree(taxonDefinition);
			Map<Integer, Taxon> idToTaxon = new HashMap<Integer, Taxon>();
			for (Taxon taxon : taxons) {
				Integer systemId = taxon.getSystemId();
				Integer parentId = taxon.getParentId();
				Taxon parent = parentId == null ? null: idToTaxon.get(parentId);
				Node newNode = tree.addNode(parent, taxon);
				List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(systemId);
				tree.addVernacularNames(newNode, vernacularNames);
				idToTaxon.put(systemId, taxon);
			}
			taxonTreeByTaxonomyIdCache.put(taxonomyId, tree);
		}
		return tree;
	}

	public boolean hasTaxons(int taxonomyId) {
		int count = taxonDao.countTaxons(taxonomyId);
		return count > 0;
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
