package org.openforis.collect.dataview;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;

public class QueryDtoConverter {

	public DataQuery fromQueryDto(CollectSurvey survey, QueryDto queryDto) {
		DataQuery query = new DataQuery(survey);
		EntityDefinition contextEntityDefn = survey.getSchema().getDefinitionById(queryDto.getContextEntityDefinitionId());
		query.setEntityDefinition(contextEntityDefn);
		
		//TODO
		return query;
	}
	
}
