package org.openforis.collect.dataview;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.RDBReportingRepositories;
import org.openforis.collect.relational.RDBReportingRepositories.Callback;
import org.openforis.collect.relational.jooq.JooqRelationalSchemaCreator;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataPrimaryKeyColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryExecutor {
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RDBReportingRepositories rdbReportingRepositories;
	
	public QueryResult runQuery(QueryDto query) {
		String surveyName = query.getSurveyName();
		CollectSurvey survey = surveyManager.get(surveyName);
		RelationalSchema relationalSchema = rdbReportingRepositories.getRelationalSchema(surveyName);
		final QueryResult result = new QueryResult();
		rdbReportingRepositories.withConnection(surveyName, query.getRecordStep(), new Callback() {
			public void execute(Connection conn) {
				JooqRDBSelector rdbSelector = new JooqRDBSelector(relationalSchema, conn);
				EntityDefinition contextEntityDef = survey.getSchema().getDefinitionById(query.getContextEntityDefinitionId());
				QueryResult queryResult = rdbSelector.runQuery(contextEntityDef, query);
				result.setTotalRecords(queryResult.getTotalRecords());
				result.setRows(queryResult.getRows());
			}
		});
		return result;
	}
	
	private static class JooqRDBSelector extends JooqRelationalSchemaCreator {

		public JooqRDBSelector(RelationalSchema relationalSchema, Connection conn) {
			super(relationalSchema, conn);
		}
		
		public QueryResult runQuery(EntityDefinition contextEntityDef, QueryDto query) {
			DataTable dataTable = schema.getDataTable(contextEntityDef);
			String viewName = getDataTableViewName(dataTable.getName());
			
			QueryResult queryResult = new QueryResult();
			Table<Record> view = DSL.table(dsl.isSchemaLess() ? DSL.name(viewName): DSL.name(schema.getName(), viewName));
			
			int totalRecords = dsl.fetchCount(view);
			queryResult.setTotalRecords(totalRecords);
			
			int offset = (query.getPage() - 1) * query.getRecordsPerPage();
			Result<Record> result = dsl
				.selectFrom(view)
				.limit(offset, query.getRecordsPerPage())
				.fetch();
			
			for (Record record : result) {
				QueryResultRow row = new QueryResultRow();
				DataPrimaryKeyColumn recordPKColumn = dataTable.getRootAncestor().getPrimaryKeyColumn();
				int recordId = record.getValue(recordPKColumn.getName(), Integer.class);
				row.setRecordId(recordId);
				for (QueryColumnDto col : query.getColumns()) {
					AttributeDefinition attrDef = contextEntityDef.getSchema().getDefinitionById(col.getAttributeDefinitionId());
					String value = extractValue(record, dataTable, attrDef);
					row.addValue(value);
				}
				queryResult.addRow(row);
			}
			return queryResult;
		}

		private List<DataColumn> getAttributeColumns(DataTable dataTableView, AttributeDefinition attrDef) {
			DataTable currentTable = dataTableView;
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
			List<DataColumn> dataColumns = getAttributeColumns(dataTable, attrDef);
			if (dataColumns.isEmpty()) {
				return null;
			} else {
				if (	   attrDef instanceof BooleanAttributeDefinition 
						|| attrDef instanceof CodeAttributeDefinition
						|| attrDef instanceof DateAttributeDefinition
						|| attrDef instanceof NumericAttributeDefinition
						|| attrDef instanceof TaxonAttributeDefinition
						|| attrDef instanceof TextAttributeDefinition
						|| attrDef instanceof TimeAttributeDefinition) {
					return record.getValue(dataColumns.get(0).getName(), String.class);
				} else {
					return null;
				}
			}
		}
	}

}
