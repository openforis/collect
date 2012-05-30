/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.expression.AbsoluteModelPathExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class SpeciesService {

	@Autowired
	private SpeciesManager taxonManager;
	
	@Autowired
	private SessionManager sessionManager;
	
	private enum SearchType {
		BY_CODE,
		BY_SCIENTIFIC_NAME,
		BY_VERNACULAR_NAME,
		BY_VERNACULAR_NAME_QUALIFIERS
	}
	
	/**
	 * @param searchString 
	 * @param maxResults 
	 */
	public List<TaxonOccurrenceProxy> findByCode(String searchString, int maxResults) {
		return find(SearchType.BY_CODE, searchString, maxResults, null);
	}

	/**
	 * 
	 * @param searchString
	 * @param maxResults
	 * @return
	 */
	public List<TaxonOccurrenceProxy> findByScientificName(String searchString, int maxResults) {
		return find(SearchType.BY_SCIENTIFIC_NAME, searchString, maxResults, null);
	}
	
	/**
	 * 
	 * @param searchString
	 * @param maxResults
	 * @return
	 */
	public List<TaxonOccurrenceProxy> findByVernacularName(int nodeId, String searchString, int maxResults) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		Node<? extends NodeDefinition> node = record.getNodeByInternalId(nodeId);
		
		TaxonAttribute attr = (TaxonAttribute) node;
		TaxonAttributeDefinition definition = attr.getDefinition();
		HashMap<String, String> hashQualifiers = new HashMap();	
		
		if (node instanceof TaxonAttribute){			
			List<String> q = definition.getQualifiers();
			if(q!=null){
				int i = 1;
				for(String qualifierExpression : q){
					String qualifierValue="";
					SurveyContext context = record.getSurveyContext();
					ExpressionFactory expressionFactory = context.getExpressionFactory();					
					try {
						AbsoluteModelPathExpression expression = expressionFactory.createAbsoluteModelPathExpression(qualifierExpression);
						CodeAttribute code = (CodeAttribute) expression.evaluate(record);
						qualifierValue = code.getValue().getCode();

					} catch (InvalidExpressionException e) {
						e.printStackTrace();
					}
					hashQualifiers.put("qualifier" + i, qualifierValue);
				}
				return find(SearchType.BY_VERNACULAR_NAME_QUALIFIERS, searchString, maxResults,hashQualifiers);
			}else{
				return find(SearchType.BY_VERNACULAR_NAME, searchString, maxResults, null);
			}
			
		} else {
			throw new IllegalArgumentException("Expected TaxonAttribute but got "+node.getClass());
		}
	}

	/**
	 * Generic find method that uses SearchType enum
	 * 
	 * @param type
	 * @param searchString
	 * @param maxResults
	 * @param hashQualifiers 
	 * @return
	 */
	private List<TaxonOccurrenceProxy> find(SearchType type, String searchString, int maxResults, HashMap<String, String> hashQualifiers) {
		List<TaxonOccurrence> list = null;
		switch(type) {
			case BY_CODE:
				list = taxonManager.findByCode(searchString, maxResults);
				break;
			case BY_SCIENTIFIC_NAME:
				list = taxonManager.findByScientificName(searchString, maxResults);
				break;
			case BY_VERNACULAR_NAME:
				list = taxonManager.findByVernacularName(searchString, maxResults, null);
				break;
			case BY_VERNACULAR_NAME_QUALIFIERS:
				list = taxonManager.findByVernacularName(searchString, maxResults, hashQualifiers);
				break;
		}
		List<TaxonOccurrenceProxy> result = new ArrayList<TaxonOccurrenceProxy>();
		if(list != null) {
			for (TaxonOccurrence o : list) {
				TaxonOccurrenceProxy proxy = new TaxonOccurrenceProxy(o);
				result.add(proxy);
			}
		}
		return result;	
	}
	
	public void suggest(TaxonOccurrence taxon) {

	}

	public void suggest(long taxonId, String taxonName) {

	}

}
