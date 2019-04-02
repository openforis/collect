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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	
	private static final Logger LOG = LogManager.getLogger(SpeciesManager.class);
	
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

	public List<CollectTaxonomy> loadTaxonomiesBySurvey(CollectSurvey survey) {
		return taxonomyDao.loadAllBySurvey(survey);
	}
	
	public CollectTaxonomy loadTaxonomyById(CollectSurvey survey, int id) {
		return taxonomyDao.loadById(survey, id);
	}

	public CollectTaxonomy loadTaxonomyByName(CollectSurvey survey, String name) {
		return taxonomyDao.loadByName(survey, name);
	}

	public List<TaxonOccurrence> findByCode(CollectTaxonomy taxonomy, String searchString, int maxResults,
			TaxonSearchParameters parameters) {
		List<Taxon> list = taxonDao.findByCode(taxonomy, parameters.getHighestRank(), searchString, maxResults);
		return fromTaxonomiesToTaxonOccurrences(list, parameters);
	}

	public List<TaxonOccurrence> findByScientificName(CollectTaxonomy taxonomy, String searchString, int maxResults, TaxonSearchParameters parameters) {
		List<Taxon> list = taxonDao.findByScientificName(taxonomy, parameters.getHighestRank(), searchString, maxResults);
		return fromTaxonomiesToTaxonOccurrences(list, parameters);
	}
	
	public List<TaxonOccurrence> findByFamilyCode(CollectTaxonomy taxonomy, String searchString, int maxResults, 
			TaxonSearchParameters parameters) {
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		TaxonTree taxonTree = loadTaxonTree(taxonomy);
		List<Taxon> families = taxonTree.findTaxaByCodeStartingWith(searchString, parameters.getHighestRank());
		result.addAll(fromTaxonomiesToTaxonOccurrences(families, parameters));
		for (Taxon familyTaxon : families) {
			List<TaxonOccurrence> descendants = taxonTree.getDescendantOccurrences(familyTaxon, parameters);
			result.addAll(descendants);
		}
		return result;
	}

	public List<TaxonOccurrence> findByFamilyScientificName(CollectTaxonomy taxonomy, String searchString, int maxResults, 
			TaxonSearchParameters parameters) {
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		TaxonTree taxonTree = loadTaxonTree(taxonomy);
		List<Taxon> families = taxonTree.findTaxaByScientificNameStartingWith(searchString, parameters.getHighestRank());
		result.addAll(fromTaxonomiesToTaxonOccurrences(families, parameters));
		for (Taxon familyTaxon : families) {
			List<TaxonOccurrence> descendants = taxonTree.getDescendantOccurrences(familyTaxon, parameters);
			result.addAll(descendants);
		}
		return result;
	}
	
	public List<TaxonOccurrence> findByVernacularName(CollectTaxonomy taxonomy, TaxonAttribute attr, String searchString, int maxResults, TaxonSearchParameters parameters) {
		List<TaxonVernacularName> list = null;
		String[] qualifierValues = null;
		if ( attr != null ) {
			qualifierValues = extractQualifierValues(attr);
		}
		if (qualifierValues == null){
			list = taxonVernacularNameDao.findByVernacularName(taxonomy.getId(), searchString, maxResults);
		} else{
			list = taxonVernacularNameDao.findByVernacularName(taxonomy.getId(), searchString, qualifierValues, maxResults);
		}
		return fromVernacularNamesToTaxonOccurrences(taxonomy, list, parameters);
	}
	
	public TaxonSummaries loadFullTaxonSummariesOld(CollectTaxonomy taxonomy) {
		TaxonTree tree = loadTaxonTree(taxonomy);
		List<TaxonSummary> summaries = tree.toSummaries(TaxonRank.GENUS, false);
		List<String> sortedVernacularNamesLanguageCodes = new ArrayList<String>(tree.getVernacularLanguageCodes());
		Collections.sort(sortedVernacularNamesLanguageCodes);
		List<String> infoAttributeNames = taxonomy.getSurvey().getReferenceDataSchema().getTaxonomyDefinition(taxonomy.getName()).getAttributeNames();
		return new TaxonSummaries(summaries.size(), summaries, sortedVernacularNamesLanguageCodes, infoAttributeNames);
	}
	
	public TaxonSummaries loadFullTaxonSummaries(CollectTaxonomy taxonomy) {
		TaxonTree tree = loadTaxonTree(taxonomy);
		List<TaxonSummary> summaries = tree.toSummaries(TaxonRank.FAMILY, true);
		List<String> sortedVernacularNamesLanguageCodes = new ArrayList<String>(tree.getVernacularLanguageCodes());
		Collections.sort(sortedVernacularNamesLanguageCodes);
		List<String> infoAttributeNames = taxonomy.getSurvey().getReferenceDataSchema().getTaxonomyDefinition(taxonomy.getName()).getAttributeNames();
		return new TaxonSummaries(summaries.size(), summaries, sortedVernacularNamesLanguageCodes, infoAttributeNames);
	}

	public TaxonSummaries loadTaxonSummaries(CollectSurvey survey, int taxonomyId) {
		return loadTaxonSummaries(survey, taxonomyId, 0, Integer.MAX_VALUE);
	}
	
	public TaxonSummaries loadTaxonSummaries(CollectSurvey survey, int taxonomyId, int offset, int maxRecords) {
		CollectTaxonomy taxonomy = loadTaxonomyById(survey, taxonomyId);
		String taxonomyName = taxonomy.getName();
		int totalCount = taxonDao.countTaxons(taxonomy);
		Set<String> vernacularNamesLanguageCodes = new HashSet<String>();
		List<TaxonSummary> items = new ArrayList<TaxonSummary>();
		TaxonomyDefinition taxonDefinition = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName);
		if ( totalCount > 0 ) {
			List<Taxon> taxons = taxonDao.loadTaxons(taxonomy, offset, maxRecords);
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
	
	public Taxon loadTaxonByCode(CollectTaxonomy taxonomy, String code) {
		TaxonTree taxonTree = loadTaxonTree(taxonomy);
		Node node = taxonTree.getNodeByCode(code);
		return node == null ? null : node.getTaxon();
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
		deleteTaxonsByTaxonomy(taxonomy);
		taxonomyDao.delete(taxonomy);
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void deleteTaxonsByTaxonomy(CollectTaxonomy taxonomy) {
		taxonVernacularNameDao.deleteByTaxonomy(taxonomy.getId());
		taxonDao.deleteByTaxonomy(taxonomy);
		taxonTreeByTaxonomyIdCache.remove(taxonomy.getId());
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
	public void insertTaxons(final CollectTaxonomy taxonomy, TaxonTree tree, boolean overwriteAll) {
		if ( overwriteAll ) {
			deleteTaxonsByTaxonomy(taxonomy);
		}
		final List<Taxon> taxonInsertBuffer = new ArrayList<Taxon>();
		final List<TaxonVernacularName> taxonVernacularNameInsertBuffer = new ArrayList<TaxonVernacularName>();
		int nextTaxonId = taxonDao.nextId(taxonomy);
		int nextTaxonVernacularNameId = taxonVernacularNameDao.nextId();
		tree.assignSystemIds(nextTaxonId, nextTaxonVernacularNameId);
		tree.depthFirstVisit(new TaxonTree.NodeVisitor() {
			public void visit(Node node) {
				persistTaxonTreeNode(taxonInsertBuffer, taxonVernacularNameInsertBuffer, taxonomy, node);
			}
		});
		flushTaxonInsertBuffer(taxonomy, taxonInsertBuffer);
		flushTaxonVernacularNameInsertBuffer(taxonVernacularNameInsertBuffer);
		
		taxonTreeByTaxonomyIdCache.remove(taxonomy.getId());
	}
	
	protected void persistTaxonTreeNode(List<Taxon> taxonInsertBuffer, List<TaxonVernacularName> taxonVernacularNameInsertBuffer, 
			CollectTaxonomy taxonomy, Node node) {
		Taxon taxon = node.getTaxon();
		taxon.setTaxonomyId(taxonomy.getId());
		taxon.setStep(CONFIRMED_TAXON_STEP_NUMBER);
		taxonInsertBuffer.add(taxon);
		if ( taxonInsertBuffer.size() > TAXON_INSERT_BUFFER_SIZE ) {
			flushTaxonInsertBuffer(taxonomy, taxonInsertBuffer);
		}
		List<TaxonVernacularName> vernacularNames = node.getVernacularNames();
		for (TaxonVernacularName vernacularName : vernacularNames) {
			taxonVernacularNameInsertBuffer.add(vernacularName);
			if ( taxonInsertBuffer.size() > TAXON_VERNACULAR_NAME_INSERT_BUFFER_SIZE ) {
				flushTaxonVernacularNameInsertBuffer(taxonVernacularNameInsertBuffer);
			}	
		}
	}

	protected void flushTaxonInsertBuffer(CollectTaxonomy taxonomy, List<Taxon> taxonInsertBuffer) {
		taxonDao.insert(taxonomy, taxonInsertBuffer);
		taxonInsertBuffer.clear();
	}

	protected void flushTaxonVernacularNameInsertBuffer(
			List<TaxonVernacularName> taxonVernacularNameInsertBuffer) {
		taxonVernacularNameDao.insert(taxonVernacularNameInsertBuffer);
		taxonVernacularNameInsertBuffer.clear();
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void delete(Taxon taxon) {
		taxonDao.delete(taxon);
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
	public void moveTaxonomies(CollectSurvey fromSurvey, CollectSurvey toSurvey) {
		deleteTaxonomiesBySurvey(toSurvey);
		List<CollectTaxonomy> taxonomies = taxonomyDao.loadAllBySurvey(fromSurvey);
		for (CollectTaxonomy taxonomy : taxonomies) {
			taxonomy.setSurvey(toSurvey);
			taxonomyDao.update(taxonomy);
		}
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void deleteTaxonomiesBySurvey(CollectSurvey survey) {
		List<CollectTaxonomy> taxonomies = taxonomyDao.loadAllBySurvey(survey);
		for (CollectTaxonomy taxonomy : taxonomies) {
			delete(taxonomy);
		}
	}
	
	@Transactional(readOnly=false, propagation=Propagation.REQUIRED)
	public void copyTaxonomy(CollectSurvey fromSurvey, CollectSurvey toSurvey) {
		List<CollectTaxonomy> taxonomies = taxonomyDao.loadAllBySurvey(fromSurvey);
		for (CollectTaxonomy oldTaxonomy : taxonomies) {
			oldTaxonomy.setSurvey(fromSurvey);
			CollectTaxonomy newTaxonomy = new CollectTaxonomy(oldTaxonomy);
			newTaxonomy.setSurvey(toSurvey);
			taxonomyDao.insert(newTaxonomy);
			copyTaxons(oldTaxonomy, newTaxonomy);
		}
	}

	protected void copyTaxons(CollectTaxonomy oldTaxonomy, CollectTaxonomy newTaxonomy) {
		int taxonIdShift = taxonDao.duplicateTaxons(oldTaxonomy, newTaxonomy);
		taxonVernacularNameDao.duplicateVernacularNames(oldTaxonomy.getId(), taxonIdShift);
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
					LOG.warn("Error evaluating taxon qualifiers: ", e);
					break;
				}
			}
		}
		return qualifierValues;
	}

	public TaxonTree loadTaxonTree(CollectTaxonomy taxonomy) {
		TaxonTree tree = taxonTreeByTaxonomyIdCache.get(taxonomy.getId());
		if (tree == null) {
			String taxonomyName = taxonomy.getName();
			TaxonomyDefinition taxonDefinition = taxonomy.getSurvey().getReferenceDataSchema().getTaxonomyDefinition(taxonomyName);
			List<Taxon> taxons = taxonDao.loadTaxonsForTreeBuilding(taxonomy);
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
			taxonTreeByTaxonomyIdCache.put(taxonomy.getId(), tree);
		}
		return tree;
	}

	public boolean hasTaxons(CollectTaxonomy taxonomy) {
		int count = taxonDao.countTaxons(taxonomy);
		return count > 0;
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
	
	private List<TaxonOccurrence> fromVernacularNamesToTaxonOccurrences(CollectTaxonomy taxonomy,
			List<TaxonVernacularName> vernacularNames, TaxonSearchParameters parameters) {
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (TaxonVernacularName vernacularName : vernacularNames) {
			Taxon taxon = taxonDao.loadById(taxonomy, vernacularName.getTaxonSystemId());
			TaxonRank highestRank = parameters.getHighestRank();
			if (highestRank == null || highestRank == taxon.getTaxonRank() || highestRank.isHigherThan(taxon.getTaxonRank())) {
				TaxonOccurrence o = new TaxonOccurrence(taxon, vernacularName);
				if (parameters.isIncludeAncestorTaxons()) {
					loadAncestorTaxons(taxon, o);
				}
				result.add(o);
			}
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
			Taxon parentTaxon = taxonDao.loadById((CollectTaxonomy) currentTaxon.getTaxonomy(), currentTaxon.getParentId());
			TaxonOccurrence parentTaxonOccurrence = new TaxonOccurrence(parentTaxon);
			o.addAncestorTaxon(parentTaxonOccurrence);
			currentTaxon = parentTaxon;
		}
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
