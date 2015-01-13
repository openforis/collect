package org.openforis.collect.datacleansing;

import java.util.Collections;
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
		return execute(query, step, 0, Integer.MAX_VALUE);
	}
	
	public DataQueryResultIterator execute(DataQuery query, Step step, int recordOffset, int maxRecords) {
		CollectSurvey survey = query.getSurvey();
		EntityDefinition entityDef = (EntityDefinition) survey.getSchema().getDefinitionById(query.getEntityDefinitionId());
		EntityDefinition rootEntityDef = entityDef.getRootEntity();
		Integer rootEntityId = rootEntityDef.getId();
		
		RecordFilter filter = new RecordFilter(survey);
		filter.setStep(step);
		filter.setRootEntityId(rootEntityId);
		List<CollectRecord> summaries = recordManager.loadSummaries(filter);
		List<CollectRecord> evaluatedSummaries;
		if (summaries.isEmpty() || recordOffset >= summaries.size()) {
			evaluatedSummaries = Collections.emptyList();
		} else {
			evaluatedSummaries = summaries.subList(recordOffset, Math.min(recordOffset + maxRecords, summaries.size()));
		}
		DataQueryEvaluator queryEvaluator = createQueryEvaluator(query);
		DataQueryResultIterator result = new DataQueryResultIterator(recordManager, evaluatedSummaries, queryEvaluator);
		return result;
	}

	private DataQueryEvaluator createQueryEvaluator(DataQuery query) {
		return new XPathDataQueryEvaluator(query);
	}
	
}
