/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	public void save(Taxonomy taxonomy) {
		if ( taxonomy.getId() == null ) {
			taxonomyDao.insert(taxonomy);
		} else {
			taxonomyDao.update(taxonomy);
		}
	}
	
	public void delete(Taxonomy taxonomy) {
		Integer id = taxonomy.getId();
		taxonDao.deleteByTaxonomy(id);
		taxonomyDao.delete(id);
	}
	
	public void save(Taxon taxon) {
		if ( taxon.getSystemId() == null ) {
			taxonDao.insert(taxon);
		} else {
			taxonDao.update(taxon);
		}
	}

	public void delete(Taxon taxon) {
		taxonDao.delete(taxon.getSystemId());
	}
	
	public void save(TaxonVernacularName vernacularName) {
		if ( vernacularName.getId() == null ) {
			taxonVernacularNameDao.insert(vernacularName);
		} else {
			taxonVernacularNameDao.update(vernacularName);
		}
	}
	
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
