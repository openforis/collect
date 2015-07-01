package org.openforis.collect.relational.jooq;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.RDBUpdater;
import org.openforis.collect.relational.data.ColumnValuePair;
import org.openforis.collect.relational.data.DataExtractor;
import org.openforis.collect.relational.data.DataExtractorFactory;
import org.openforis.collect.relational.data.Row;
import org.openforis.collect.relational.data.internal.DataTableDataExtractor;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataAncestorFKColumn;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class JooqDatabaseExporter implements RDBUpdater, DatabaseExporter {

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
	public void insertReferenceData(RelationalSchema schema) {
		BatchQueryExecutor batchExecutor = new BatchQueryExecutor(schema);
		for (CodeTable codeTable : schema.getCodeListTables()) {
			DataExtractor extractor = DataExtractorFactory.getExtractor(codeTable);
			batchExecutor.addInserts(extractor);
		}
		batchExecutor.flush();
	}

	@Override
	public void insertData(RelationalSchema schema, CollectRecord record) {
		BatchQueryExecutor batchExecutor = new BatchQueryExecutor(schema);
		for (DataTable table : schema.getDataTables()) {
			DataExtractor extractor = DataExtractorFactory.getRecordDataExtractor(table, record);
			batchExecutor.addInserts(extractor);
		}
		batchExecutor.flush();
	}

	@Override
	public void updateData(RelationalSchema schema, CollectRecord record) {
		deleteData(schema, record.getId(), record.getRootEntity().getDefinition().getId());
		insertData(schema, record);
	}
	
	@Override
	public void updateData(RelationalSchema rdbSchema, DataTable dataTable,
			BigInteger pkValue,
			List<ColumnValuePair<DataColumn, ?>> columnValuePairs) {
		BatchQueryExecutor batchExecutor = new BatchQueryExecutor(rdbSchema);
		batchExecutor.addUpdate(dataTable, pkValue, columnValuePairs);
	}
	
	@Override
	public void deleteData(RelationalSchema schema, int recordId, int rootDefId) {
		deleteDataForEntity(schema, recordId, recordId, rootDefId);
	}
	
	@Override
	public void deleteDataForEntity(RelationalSchema schema, int recordId, int entityId, int entityDefinitionId) {
		DataTable tableToDeleteFor = schema.getDataTableByDefinitionId(entityDefinitionId);
		EntityDefinition entityDefToDeleteFor = (EntityDefinition) tableToDeleteFor.getNodeDefinition();
		BigInteger pkValue = DataTableDataExtractor.getTableArtificialPK(recordId, entityDefToDeleteFor, entityId);
		
		BatchQueryExecutor batchExecutor = new BatchQueryExecutor(schema);
		
		//delete data from the actual table
		batchExecutor.addDelete(tableToDeleteFor, tableToDeleteFor.getPrimaryKeyColumn(), pkValue);

		//delete data from descendant tables
		List<DataTable> descendantTables = new ArrayList<DataTable>(schema.getDescendantTablesForDefinition(entityDefinitionId));
		Collections.reverse(descendantTables);
		for (DataTable dataTable : descendantTables) {
			DataAncestorFKColumn ancestorIdColumn = dataTable.getAncestorIdColumn(entityDefinitionId);
			batchExecutor.addDelete(dataTable, ancestorIdColumn, pkValue);
		}
		batchExecutor.flush();
	}
	
	private class BatchQueryExecutor {
		
		private static final int BATCH_MAX_SIZE = 10000;
		
		private RelationalSchema schema;
		private List<Query> queries;
		
		public BatchQueryExecutor(RelationalSchema schema) {
			this.schema = schema;
			this.queries = new ArrayList<Query>();
		}

		public void addInserts(DataExtractor extractor) {
			while(extractor.hasNext()) {
				Row row = extractor.next();
				addInsert(row);
			}
		}

		public void addInsert(Row row) {
			queries.add(createInsertQuery(row));
			if ( queries.size() == BATCH_MAX_SIZE ) {
				flush();
			}
		}

		public void addUpdate(Table<?> table, BigInteger pkValue, List<ColumnValuePair<DataColumn, ?>> columnValuePairs) {
			Map<Field<?>, Object> fieldToValue = new HashMap<Field<?>, Object>();
			for (ColumnValuePair<DataColumn, ?> columnValuePair : columnValuePairs) {
				Field<?> field = DSL.field(columnValuePair.getColumn().getName());
				Object value = columnValuePair.getValue();
				fieldToValue.put(field, value);
			}
			@SuppressWarnings("unchecked")
			Column<BigInteger> pkColumn = (Column<BigInteger>) table.getPrimaryKeyConstraint().getColumns().get(0);
			Field<Object> pkField = DSL.field(pkColumn.getName());
			UpdateConditionStep<Record> query = dsl.update(getJooqTable(table)).set(fieldToValue).where(pkField.eq(pkValue));
			queries.add(query);
		}
		
		public void addDelete(Table<?> table, Column<?> pkColumn, BigInteger pkValue) {
			queries.add(createDeleteQuery(table, pkColumn, pkValue));
		}

		public void flush() {
			if ( queries.isEmpty() ) {
				return;
			}
			try {
				dsl.batch(queries).execute();
				queries.clear();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		private InsertQuery<Record> createInsertQuery(Row row) {
			Table<?> table = row.getTable();
			InsertQuery<Record> insert = dsl.insertQuery(getJooqTable(table));
			List<Object> values = row.getValues();
			List<Column<?>> cols = table.getColumns();
			for (int i = 0; i < cols.size(); i++) {
				Object val = values.get(i);
				if ( val != null ) {
					String col = cols.get(i).getName();
					insert.addValue(DSL.fieldByName(col), val);
				}
			}
			return insert;
		}
		
		private DeleteQuery<Record> createDeleteQuery(Table<?> table, Column<?> pkColumn, BigInteger pkValue) {
			org.jooq.Table<Record> jooqTable = getJooqTable(table);
			Field<BigInteger> jooqPKColumn = DSL.field(pkColumn.getName(), BigInteger.class);
			DeleteQuery<Record> query = dsl.deleteQuery(jooqTable);
			query.addConditions(jooqPKColumn.equal(pkValue));
			return query;
		}
		
		private org.jooq.Table<Record> getJooqTable(Table<?> table) {
			if ( isSchemaLess() ) {
				return DSL.tableByName(table.getName());
			} else {
				return DSL.tableByName(schema.getName(), table.getName());
			}
		}
		
		private boolean isSchemaLess() {
			return dsl.configuration().dialect() == SQLDialect.SQLITE;
		}

	}
	
}
