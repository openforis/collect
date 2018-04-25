package org.openforis.collect.dataview;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.RDBReportingRepositories;
import org.openforis.collect.relational.RDBReportingRepositories.Callback;
import org.openforis.collect.relational.jooq.JooqRelationalSchemaCreator;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryExecutor {
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RDBReportingRepositories rdbReportingRepositories;
	
	public QueryResult runQuery(Query query) {
		String surveyName = query.getSurveyName();
		CollectSurvey survey = surveyManager.get(surveyName);
		RelationalSchema relationalSchema = rdbReportingRepositories.getRelationalSchema(surveyName);
		final QueryResult result = new QueryResult();
		rdbReportingRepositories.withConnection(surveyName, query.getRecordStep(), new Callback() {
			public void execute(Connection conn) {
				JooqRDBSelector rdbSelector = new JooqRDBSelector(relationalSchema, conn);
				EntityDefinition contextEntityDef = survey.getSchema().getDefinitionById(query.getContextEntityDefinitionId());
				QueryResult queryResult = rdbSelector.queryView(contextEntityDef, query.getColumns());
				result.setRows(queryResult.getRows());
			}
		});
		return result;
	}
	
	private static class JooqRDBSelector extends JooqRelationalSchemaCreator {

		public JooqRDBSelector(RelationalSchema relationalSchema, Connection conn) {
			super(relationalSchema, conn);
		}
		
		public QueryResult queryView(EntityDefinition contextEntityDef, List<QueryColumn> columns) {
			DataTable dataTable = schema.getDataTable(contextEntityDef);
			String viewName = getDataTableViewName(dataTable.getName());
			QueryResult queryResult = new QueryResult();
			Result<Record> result = dsl.selectFrom(DSL.table(dsl.isSchemaLess() ? DSL.name(viewName): DSL.name(schema.getName(), viewName)))
				.fetch();
			for (Record record : result) {
				QueryResultRow row = new QueryResultRow();
				for (QueryColumn col : columns) {
					AttributeDefinition attrDef = contextEntityDef.getSchema().getDefinitionById(col.getAttributeDefinitionId());
					String value = extractValue(record, dataTable, attrDef);
					row.putValueByDefinitionId(col.getAttributeDefinitionId(), value);
				}
				queryResult.addRow(row);
			}
			return queryResult;
		}

		private List<DataColumn> getDataTableViewColumns(DataTable dataTable, AttributeDefinition attrDef) {
			DataTable currentTable = dataTable;
			while(currentTable != null) {
				List<DataColumn> columns = currentTable.getDataColumns(attrDef);
				if (! columns.isEmpty()) {
					return columns;
				}
				currentTable = currentTable.getParent();
			}
			return Collections.emptyList();
		}

		private String extractValue(Record record, DataTable dataTable, AttributeDefinition attrDef) {
			List<DataColumn> dataColumns = getDataTableViewColumns(dataTable, attrDef);
			if (attrDef instanceof BooleanAttributeDefinition 
					|| attrDef instanceof CodeAttributeDefinition
					|| attrDef instanceof NumericAttributeDefinition) {
				return record.getValue(dataColumns.get(0).getName(), String.class);
			} else {
				return null;
			}
		}
	}

}
