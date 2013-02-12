/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.collect.model.CollectSurveyContext;
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
	private CollectSurveyContext surveyContext;

	@Transactional
	public List<Taxonomy> loadAllTaxonomies() {
		return taxonomyDao.loadAll();
	}
	
	@Transactional
	public Taxonomy loadTaxonomyById(int id) {
		return taxonomyDao.loadById(id);
	}

	@Transactional
	public Taxonomy loadTaxonomyByName(String name) {
		return taxonomyDao.load(name);
	}

	@Transactional
	public List<TaxonOccurrence> findByCode(String taxonomyName, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.load(taxonomyName);
		List<Taxon> list = taxonDao.findByCode(taxonomy.getId(), searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon.getTaxonId(), taxon.getCode(), taxon.getScientificName());
			result.add(o);
		}
		return result;
	}

	@Transactional
	public List<TaxonOccurrence> findByScientificName(String taxonomyName, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.load(taxonomyName);
		List<Taxon> list = taxonDao.findByScientificName(taxonomy.getId(), searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon.getTaxonId(), taxon.getCode(), taxon.getScientificName());
			result.add(o);
		}
		return result;
	}

	@Transactional
	public List<TaxonOccurrence> findByVernacularName(String taxonomyName, String searchString, int maxResults) {
		return findByVernacularName(taxonomyName, null, searchString, maxResults);
	}
	
	@Transactional
	public List<TaxonOccurrence> findByVernacularName(String taxonomyName, TaxonAttribute attr, String searchString, int maxResults) {
		List<TaxonVernacularName> list = null;
		Taxonomy taxonomy = taxonomyDao.load(taxonomyName);
		Integer taxonomyId = taxonomy.getId();
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
	public TaxonSummaries loadTaxonSummaries(int taxonomyId, int offset, int maxRecords) {
		int totalCount = taxonDao.countTaxons(taxonomyId);
		Set<String> vernacularNamesLanguageCodes = new HashSet<String>();
		List<TaxonSummary> items = new ArrayList<TaxonSummary>();
		if ( totalCount > 0 ) {
			List<Taxon> taxons = taxonDao.loadTaxons(taxonomyId, offset, maxRecords);
			for (Taxon taxon : taxons) {
				TaxonSummary summary = createSummary(taxon);
				Set<String> itemVernLangCodes = summary.getLanguageToVernacularNames().keySet();
				vernacularNamesLanguageCodes.addAll(itemVernLangCodes);
				items.add(summary);
			}
		}
		TaxonSummaries result = new TaxonSummaries(totalCount, items, vernacularNamesLanguageCodes);
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
	public void save(Taxonomy taxonomy) {
		if ( taxonomy.getId() == null ) {
			taxonomyDao.insert(taxonomy);
		} else {
			taxonomyDao.update(taxonomy);
		}
	}
	
	@Transactional
	public void delete(Taxonomy taxonomy) {
		Integer id = taxonomy.getId();
		deleteTaxonsByTaxonomy(taxonomy);
		taxonomyDao.delete(id);
	}

	@Transactional
	public void deleteTaxonsByTaxonomy(Taxonomy taxonomy) {
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
			ExpressionFactory expressionFactory = surveyContext.getExpressionFactory();
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

}
