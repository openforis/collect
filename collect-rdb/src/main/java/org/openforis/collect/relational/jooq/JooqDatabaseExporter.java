package org.openforis.collect.relational.jooq;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.DatabaseUpdater;
import org.openforis.collect.relational.data.DataExtractor;
import org.openforis.collect.relational.data.Row;
import org.openforis.collect.relational.data.internal.CodeTableDataExtractor;
import org.openforis.collect.relational.data.internal.DataTableDataExtractor;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class JooqDatabaseExporter implements DatabaseUpdater {

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
	public void insertReferenceData(RelationalSchema schema) throws CollectRdbException {
		BatchInsertExecutor batchExecutor = new BatchInsertExecutor(schema);
		for (CodeTable codeTable : schema.getCodeListTables()) {
			CodeTableDataExtractor extractor = new CodeTableDataExtractor(codeTable);
			batchExecutor.addInserts(extractor);
		}
		batchExecutor.flush();
	}

	@Override
	public void insertData(RelationalSchema schema, CollectRecord record) throws CollectRdbException  {
		BatchInsertExecutor batchExecutor = new BatchInsertExecutor(schema);
		for (DataTable table : schema.getDataTables()) {
			DataTableDataExtractor extractor = new DataTableDataExtractor(table, record);
			batchExecutor.addInserts(extractor);
		}
		batchExecutor.flush();
	}

	@Override
	public void updateData(RelationalSchema schema, CollectRecord record)
			throws CollectRdbException {
		deleteData(schema, record);
		insertData(schema, record);
	}
	
	@Override
	public void deleteData(RelationalSchema schema, CollectRecord record)
			throws CollectRdbException {
		// TODO Auto-generated method stub
		
	}
	
	private class BatchInsertExecutor {
		
		private static final int BATCH_MAX_SIZE = 10000;
		
		private List<InsertQuery<Record>> buffer;
		private RelationalSchema schema;
		
		public BatchInsertExecutor(RelationalSchema schema) {
			this.schema = schema;
			this.buffer = new ArrayList<InsertQuery<Record>>();
		}

		public void addInserts(DataExtractor extractor) {
			while(extractor.hasNext()) {
				Row row = extractor.next();
				addInsert(row);
			}
		}

		public void addInsert(Row row) {
			buffer.add(createInsertQuery(schema, row));
			if ( buffer.size() == BATCH_MAX_SIZE ) {
				flush();
			}
		}

		public void flush() {
			if ( buffer.isEmpty() ) {
				return;
			}
			try {
				dsl.batch(buffer).execute();
				buffer.clear();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		private InsertQuery<Record> createInsertQuery(RelationalSchema schema, Row row) {
			Table<?> table = row.getTable();
			InsertQuery<Record> insert = dsl.insertQuery(getJooqTable(schema, table));
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
		
		private org.jooq.Table<Record> getJooqTable(RelationalSchema schema, Table<?> table) {
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
