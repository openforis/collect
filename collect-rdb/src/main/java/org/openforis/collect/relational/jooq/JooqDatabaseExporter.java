package org.openforis.collect.relational.jooq;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.openforis.collect.relational.data.internal.DataTableDataExtractor.getTableArtificialPK;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.InsertSetMoreStep;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.Update;
import org.jooq.UpdateConditionStep;
import org.jooq.exception.DataAccessException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.RDBUpdater;
import org.openforis.collect.relational.data.ColumnValuePair;
import org.openforis.collect.relational.data.DataExtractor;
import org.openforis.collect.relational.data.DataExtractorFactory;
import org.openforis.collect.relational.data.Row;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataAncestorFKColumn;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataPrimaryKeyColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;
import org.openforis.concurrency.ProgressListener;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class JooqDatabaseExporter implements RDBUpdater, DatabaseExporter {
	
	private static final Log LOG = LogFactory.getLog(JooqDatabaseExporter.class);
	
	private DSLContext dsl;
	
	public JooqDatabaseExporter(Connection connection) {
		this(new CollectDSLContext(connection));
	}

	public JooqDatabaseExporter(Configuration conf) {
		this(new CollectDSLContext(conf));
	}
	
	public JooqDatabaseExporter(DSLContext dsl) {
		this.dsl = dsl;
	}

	@Override
	public void insertReferenceData(RelationalSchema schema, ProgressListener progressListener) {
		BatchQueryExecutor batchExecutor = new BatchQueryExecutor(schema, ProgressListener.NULL_PROGRESS_LISTENER);
		List<CodeTable> codeListTables = schema.getCodeListTables();
		long totalItems = codeListTables.size();
		long processedItems = 0;
		for (CodeTable codeTable : codeListTables) {
			DataExtractor extractor = DataExtractorFactory.getExtractor(codeTable);
			batchExecutor.addInserts(extractor);
			processedItems++;
			progressListener.progressMade(processedItems, totalItems);
		}
		batchExecutor.flush();
	}

	@Override
	public void insertRecordData(RelationalSchema schema, CollectRecord record, ProgressListener progressListener) {
		BatchQueryExecutor batchExecutor = new BatchQueryExecutor(schema, progressListener);
		for (DataTable table : schema.getDataTables()) {
			DataExtractor extractor = DataExtractorFactory.getRecordDataExtractor(table, record);
			batchExecutor.addInserts(extractor);
		}
		batchExecutor.flush();
	}

	@Override
	public void insertEntity(RelationalSchema schema, int recordId, 
			Integer parentId, int entityId, int entityDefinitionId) {
		insertNode(schema, recordId, parentId, entityId, entityDefinitionId);
	}

	@Override
	public void insertAttribute(RelationalSchema schema, int recordId,
			Integer parentId, int attributeId, int attributeDefinitionId) {
		insertNode(schema, recordId, parentId, attributeId, attributeDefinitionId);
	}
	
	private void insertNode(RelationalSchema schema, int recordId,
			Integer parentId, int nodeId, int nodeDefinitionId) {
		DataTable table = schema.getDataTableByDefinitionId(nodeDefinitionId);
		NodeDefinition nodeDef = table.getNodeDefinition();
		BigInteger pkValue = getTableArtificialPK(recordId, nodeDef, nodeId);
		
		QueryCreator queryCreator = new QueryCreator(dsl, schema.getName());
		DataPrimaryKeyColumn pkColumn = table.getPrimaryKeyColumn();
		org.jooq.Table<Record> jooqTable = queryCreator.getJooqTable(table);
		InsertSetMoreStep<Record> insert = dsl
				.insertInto(jooqTable)
				.set(field(pkColumn.getName()), pkValue);
		
		if (parentId != null) {
			Map<String, BigInteger> ancestorFKByColumnName = findAncestorFKByColumnName(schema, table, recordId, parentId);
			for (Entry<String, BigInteger> entry : ancestorFKByColumnName.entrySet()) {
				insert.set(field(entry.getKey()), entry.getValue());
			}
		}
		try {
			insert.execute();
		} catch (DataAccessException e) {
			if (JooqDaoSupport.isConstraintViolation(e)) {
				LOG.warn(String.format("Duplicate node already inserted: survey = %s, node path = %s, record id = %d, parent id = %d, node id = %d", 
						schema.getSurvey().getName(), nodeDef.getPath(), recordId, parentId, nodeId));
			} else {
				throw new DataAccessException("Failed to insert node into RDB", e);
			}
		}
	}
	private Map<String, BigInteger> findAncestorFKByColumnName(RelationalSchema schema, DataTable table, int recordId, int parentId) {
		Map<String, BigInteger> result = new HashMap<String, BigInteger>();
		
		DataTable parentTable = table.getParent();
		
		List<DataAncestorFKColumn> parentAncestorFKColumns = new ArrayList<DataAncestorFKColumn>(parentTable.getAncestorFKColumns());
		
		List<Field<?>> parentAncestorColumns = toFields(parentAncestorFKColumns);
		
		BigInteger parentPKValue = getTableArtificialPK(recordId, parentTable.getNodeDefinition(), parentId);
		DataPrimaryKeyColumn parentPKColumn = parentTable.getPrimaryKeyColumn();		
		QueryCreator queryCreator = new QueryCreator(dsl, schema.getName());
		SelectConditionStep<Record> selectAncestorFKs = dsl
			.select(parentAncestorColumns)
			.from(queryCreator.getJooqTable(parentTable))
			.where(field(parentPKColumn.getName()).eq(parentPKValue));
		Record record = selectAncestorFKs.fetchOne();
		
		for (int i = 0; i < parentAncestorColumns.size(); i++) {
			Field<?> parentAncestorField = parentAncestorColumns.get(i);
			
			DataAncestorFKColumn parentColumn = parentAncestorFKColumns.get(i);
			int ancestorDefinitionId = parentColumn.getAncestorDefinitionId();
			DataAncestorFKColumn column = table.getAncestorFKColumn(ancestorDefinitionId);
			BigInteger ancestorPK = record.getValue(parentAncestorField, BigInteger.class);
			result.put(column.getName(), ancestorPK);
		}
		result.put(table.getParentFKColumn().getName(), parentPKValue);

		return result;
	}
	
	private List<Field<?>> toFields(List<? extends Column<?>> columns) {
		List<Field<?>> ancestorColumns = new ArrayList<Field<?>>(columns.size());
		for (Column<?> column : columns) {
			ancestorColumns.add(field(column.getName()));
		}
		return ancestorColumns;
	}
	
	@Override
	public void replaceRecordData(RelationalSchema schema, CollectRecord record, ProgressListener progressListener) {
		deleteRecordData(schema, record.getId(), record.getRootEntity().getDefinition().getId());
		insertRecordData(schema, record, progressListener);
	}
	
	@Override
	public void updateEntityData(RelationalSchema rdbSchema, DataTable dataTable,
			BigInteger pkValue,
			List<ColumnValuePair<DataColumn, ?>> columnValuePairs) {
		BatchQueryExecutor batchExecutor = new BatchQueryExecutor(rdbSchema);
		batchExecutor.addUpdate(dataTable, pkValue, columnValuePairs);
		batchExecutor.flush();
	}
	
	@Override
	public void deleteRecordData(RelationalSchema schema, int recordId, int rootDefId) {
		deleteEntity(schema, recordId, recordId, rootDefId);
	}
	
	@Override
	public void deleteEntity(RelationalSchema schema, int recordId, int entityId, int entityDefinitionId) {
		DataTable tableToDeleteFor = schema.getDataTableByDefinitionId(entityDefinitionId);
		EntityDefinition entityDefToDeleteFor = (EntityDefinition) tableToDeleteFor.getNodeDefinition();
		BigInteger pkValue = getTableArtificialPK(recordId, entityDefToDeleteFor, entityId);
		
		BatchQueryExecutor batchExecutor = new BatchQueryExecutor(schema);
		
		//delete data from the actual table
		batchExecutor.addDelete(tableToDeleteFor, tableToDeleteFor.getPrimaryKeyColumn(), pkValue);

		//delete data from descendant tables
		List<DataTable> descendantTables = new ArrayList<DataTable>(schema.getDescendantTablesForDefinition(entityDefinitionId));
		Collections.reverse(descendantTables);
		for (DataTable dataTable : descendantTables) {
			DataAncestorFKColumn ancestorIdColumn = dataTable.getAncestorFKColumn(entityDefinitionId);
			batchExecutor.addDelete(dataTable, ancestorIdColumn, pkValue);
		}
		batchExecutor.flush();
	}
	
	@Override
	public void deleteAttribute(RelationalSchema schema, int recordId,
			int attributeId, int definitionId) {
		DataTable tableToDeleteFor = schema.getDataTableByDefinitionId(definitionId);
		NodeDefinition defToDeleteFor = tableToDeleteFor.getNodeDefinition();
		BigInteger pkValue = getTableArtificialPK(recordId, defToDeleteFor, attributeId);
		
		BatchQueryExecutor batchExecutor = new BatchQueryExecutor(schema);
		batchExecutor.addDelete(tableToDeleteFor, tableToDeleteFor.getPrimaryKeyColumn(), pkValue);
		batchExecutor.flush();
	}
	
	private class BatchQueryExecutor {
		
		private static final int BATCH_MAX_SIZE = 500;
		
		private List<Query> queries;
		private QueryCreator queryCreator;
		private ProgressListener progressListener;
		private long processedQueries;
		
		public BatchQueryExecutor(RelationalSchema schema) {
			this(schema, null);
		}
		
		public BatchQueryExecutor(RelationalSchema schema, ProgressListener progressListener) {
			this.progressListener = progressListener;
			this.queries = new ArrayList<Query>();
			this.queryCreator = new QueryCreator(dsl, schema.getName());
		}

		public void addInserts(DataExtractor extractor) {
			while(extractor.hasNext()) {
				Row row = extractor.next();
				addInsert(row);
			}
		}

		public void addInsert(Row row) {
			addQuery(queryCreator.createInsertQuery(row));
		}

		public void addUpdate(DataTable table, BigInteger pkValue, List<ColumnValuePair<DataColumn, ?>> columnValuePairs) {
			Map<Field<?>, Object> fieldToValue = new HashMap<Field<?>, Object>(columnValuePairs.size() );
			for (ColumnValuePair<DataColumn, ?> columnValuePair : columnValuePairs) {
				Field<?> field = field(columnValuePair.getColumn().getName());
				Object value = columnValuePair.getValue();
				fieldToValue.put(field, value);
			}
			Update<Record> query = queryCreator.createUpdateQuery(table, pkValue, fieldToValue);
			addQuery(query);
		}

		public void addDelete(Table<?> table, Column<?> pkColumn, BigInteger pkValue) {
			addQuery(queryCreator.createDeleteQuery(table, pkColumn, pkValue));
		}

		private void addQuery(Query query) {
			queries.add(query);
			if ( queries.size() == BATCH_MAX_SIZE ) {
				flush();
			}
		}

		public void flush() {
			if ( queries.isEmpty() ) {
				return;
			}
			try {
				dsl.batch(queries).execute();
				processedQueries += queries.size();
				queries.clear();
				notifyProgressListener();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}

		private void notifyProgressListener() {
			if (progressListener != null) {
				progressListener.progressMade(processedQueries, 0);
			}
		}
	}
	
	private class QueryCreator {
		
		private final DSLContext dsl;
		private final String schemaName;
		
		public QueryCreator(DSLContext dsl, String schemaName) {
			super();
			this.dsl = dsl;
			this.schemaName = schemaName;
		}

		public InsertQuery<Record> createInsertQuery(Row row) {
			Table<?> table = row.getTable();
			InsertQuery<Record> insert = dsl.insertQuery(getJooqTable(table));
			List<Object> values = row.getValues();
			List<Column<?>> cols = table.getColumns();
			for (int i = 0; i < cols.size(); i++) {
				Object val = values.get(i);
				if ( val != null ) {
					String col = cols.get(i).getName();
					insert.addValue(field(name(col)), val);
				}
			}
			return insert;
		}
		
		public Update<Record> createUpdateQuery(DataTable table,
				BigInteger pkValue, Map<Field<?>, Object> fieldToValue) {
			DataPrimaryKeyColumn pkColumn = table.getPrimaryKeyColumn();
			Field<Object> pkField = field(pkColumn.getName());
			UpdateConditionStep<Record> query = dsl.update(getJooqTable(table)).set(fieldToValue).where(pkField.eq(pkValue));
			return query;
		}
		
		public DeleteQuery<Record> createDeleteQuery(Table<?> table, Column<?> pkColumn, BigInteger pkValue) {
			org.jooq.Table<Record> jooqTable = getJooqTable(table);
			Field<BigInteger> jooqPKColumn = field(pkColumn.getName(), BigInteger.class);
			DeleteQuery<Record> query = dsl.deleteQuery(jooqTable);
			query.addConditions(jooqPKColumn.equal(pkValue));
			return query;
		}
		
		private org.jooq.Table<Record> getJooqTable(Table<?> table) {
			if ( isSchemaLess() ) {
				return table(name(table.getName()));
			} else {
				return table(schemaName, table.getName());
			}
		}
		
		private boolean isSchemaLess() {
			return dsl.configuration().dialect() == SQLDialect.SQLITE;
		}
		
	}
	
}
