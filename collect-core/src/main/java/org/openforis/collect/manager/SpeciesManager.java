/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonVernacularNameDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.expression.AbsoluteModelPathExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 * @author M. Togna
 * @author E. Wibowo
 * 
 */
public class SpeciesManager {

	@Autowired
	private TaxonDao taxonDao;

	@Autowired
	private TaxonVernacularNameDao taxonVernacularNameDao;

	@SuppressWarnings("unused")
	@Autowired
	private TaxonomyDao taxonomyDao;

	@Transactional
	public List<TaxonOccurrence> findByCode(String searchString, int maxResults) {
		List<Taxon> list = taxonDao.findByCode(searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon.getCode(), taxon.getScientificName());
			result.add(o);
		}
		return result;
	}

	@Transactional
	public List<TaxonOccurrence> findByScientificName(String searchString, int maxResults) {
		List<Taxon> list = taxonDao.findByScientificName(searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon.getCode(), taxon.getScientificName());
			result.add(o);
		}
		return result;
	}

	@Transactional
	public List<TaxonOccurrence> findByVernacularName(String searchString, int maxResults, CollectRecord record, Node<? extends NodeDefinition> node) {
		
		TaxonAttribute attr = (TaxonAttribute) node;
		TaxonAttributeDefinition definition = attr.getDefinition();
		HashMap<String, String> hashQualifiers = new HashMap<String, String>();	
		List<TaxonVernacularName> list;
		
		if (node instanceof TaxonAttribute){			
			List<String> q = definition.getQualifiers();
			if(q != null){
				int i = 1;
				for(String qualifierExpression : q){
					String qualifierValue="";
					SurveyContext context = record.getSurveyContext();
					ExpressionFactory expressionFactory = context.getExpressionFactory();
					
					try {
						CodeAttribute code = null;
						ModelPathExpression expression = expressionFactory.createModelPathExpression(qualifierExpression);
						code = (CodeAttribute) expression.evaluate(node, null);
						qualifierValue = code.getValue().getCode();
						hashQualifiers.put("qualifier" + i, qualifierValue);
					} catch (Exception e) {
						hashQualifiers.clear();						
						break;
					}
				}
				
				//if anything happened, ignore qualifier
				if(hashQualifiers.size()>0){
					list = taxonVernacularNameDao.findByVernacularName(searchString, maxResults, hashQualifiers);
				}else{
					list = taxonVernacularNameDao.findByVernacularName(searchString, maxResults, null);
				}
			}else{
				list = taxonVernacularNameDao.findByVernacularName(searchString, maxResults, null);
			}
			
		} else {
			throw new IllegalArgumentException("Expected TaxonAttribute but got "+node.getClass());
		}
		
		
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (TaxonVernacularName taxonVernacularName : list) {
			Integer taxonId = taxonVernacularName.getTaxonSystemId();
			Taxon taxon = taxonDao.loadById(taxonId);
			TaxonOccurrence o = new TaxonOccurrence(taxon.getCode(), taxon.getScientificName(), taxonVernacularName.getVernacularName(), taxonVernacularName.getLanguageCode(),
					taxonVernacularName.getLanguageVariety());
			result.add(o);
		}
		return result;
	}

}
