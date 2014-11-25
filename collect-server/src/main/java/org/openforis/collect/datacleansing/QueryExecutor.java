package org.openforis.collect.datacleansing;

import java.util.List;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.idm.metamodel.EntityDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class QueryExecutor {

	@Autowired
	private RecordManager recordManager;
	
	public QueryResultIterator execute(Query query) {
		CollectSurvey survey = query.getSurvey();
		EntityDefinition entityDef = (EntityDefinition) survey.getSchema().getDefinitionById(query.getEntityDefinitionId());
		EntityDefinition rootEntityDef = entityDef.getRootEntity();
		Integer rootEntityId = rootEntityDef.getId();
		Step step = query.getStep();
		
		RecordFilter filter = new RecordFilter(survey);
		filter.setStep(step);
		filter.setRootEntityId(rootEntityId);
		List<CollectRecord> summaries = recordManager.loadSummaries(filter);
		QueryEvaluator queryEvaluator = createQueryEvaluator(query);
		
		QueryResultIterator result = new QueryResultIterator(recordManager, summaries, query, queryEvaluator);
		return result;
	}

	private QueryEvaluator createQueryEvaluator(Query query) {
		return new XPathQueryEvaluator(query);
	}
	
}
