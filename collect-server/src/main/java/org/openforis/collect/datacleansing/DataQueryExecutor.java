package org.openforis.collect.datacleansing;

import java.util.List;

import org.openforis.collect.datacleansing.xpath.XPathDataQueryEvaluator;
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
public class DataQueryExecutor {

	@Autowired
	private RecordManager recordManager;
	
	public DataQueryResultIterator execute(DataQuery query, Step step) {
		CollectSurvey survey = query.getSurvey();
		EntityDefinition entityDef = (EntityDefinition) survey.getSchema().getDefinitionById(query.getEntityDefinitionId());
		EntityDefinition rootEntityDef = entityDef.getRootEntity();
		Integer rootEntityId = rootEntityDef.getId();
		
		RecordFilter filter = new RecordFilter(survey);
		filter.setStep(step);
		filter.setRootEntityId(rootEntityId);
		List<CollectRecord> summaries = recordManager.loadSummaries(filter);
		DataQueryEvaluator queryEvaluator = createQueryEvaluator(query);
		
		DataQueryResultIterator result = new DataQueryResultIterator(recordManager, summaries, queryEvaluator);
		return result;
	}

	private DataQueryEvaluator createQueryEvaluator(DataQuery query) {
		return new XPathDataQueryEvaluator(query);
	}
	
}
