package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.validation.Check;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQueryGenerator {
	
	private static final String MISSING_DATA_QUERY_TITLE_FORMAT = "Missing data for '%s' attribute";
	private static final String MISSING_DATA_CONDITION_FORMAT = "idm:blank(%s)";

	public List<DataQuery> generateMissingDataQueries(final CollectSurvey survey, final DataQueryType type) {
		final List<DataQuery> result = new ArrayList<DataQuery>();
		survey.getSchema().traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				if (def instanceof AttributeDefinition) {
					if (StringUtils.isNotBlank(def.getMinCountExpression())) {
						DataQuery query = new DataQuery(survey);
						query.setTitle(String.format(MISSING_DATA_QUERY_TITLE_FORMAT, def.getName()));
						query.setAttributeDefinition((AttributeDefinition) def);
						query.setEntityDefinition(def.getParentEntityDefinition());
						query.setConditions(String.format(MISSING_DATA_CONDITION_FORMAT, def.getName()));
						query.setType(type);
						result.add(query);
					}
				}
			}
		});
		return result;
	}
	
	public List<DataQuery> generateValidationCheckQueries(CollectSurvey survey) {
		final List<DataQuery> result = new ArrayList<DataQuery>();
		survey.getSchema().traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				if (def instanceof AttributeDefinition) {
					for (Check<?> check : ((AttributeDefinition) def).getChecks()) {
						DataQuery query = createQuery(def, check);
						result.add(query);
					}
				}
			}
		});
		return result;
	}

	private DataQuery createQuery(NodeDefinition def, Check<?> check) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(check.getCondition())) {
			sb.append(check.getCondition());
			sb.append(" and ");
		}
		sb.append(check.getExpression());
		CollectSurvey survey = (CollectSurvey) def.getSurvey();
		DataQuery query = new DataQuery(survey);
		//TODO
		query.setTitle("");
		query.setAttributeDefinition((AttributeDefinition) def);
		query.setEntityDefinition(def.getParentEntityDefinition());
		query.setConditions(sb.toString());
//		query.setType(type);
		return query;
	}
	
}
