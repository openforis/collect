package org.openforis.collect.relational.jooq;

import java.util.ArrayList;
import java.util.List;

import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.data.CodeTableDataExtractor;
import org.openforis.collect.relational.data.DataExtractor;
import org.openforis.collect.relational.data.DataTableDataExtractor;
import org.openforis.collect.relational.data.Row;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;

/**
 * 
 * @author G. Miceli
 *
 */
public class JooqDatabaseExporter implements DatabaseExporter {

	private Factory create;
	
	public JooqDatabaseExporter(Factory create) {
		super();
		this.create = create;
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
				create.batch(buffer).execute();
				buffer.clear();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		private InsertQuery<Record> createInsertQuery(RelationalSchema schema, Row row) {
			Table<?> table = row.getTable();
			InsertQuery<Record> insert = create.insertQuery(getJooqTable(schema, table));
			List<Object> values = row.getValues();
			List<Column<?>> cols = table.getColumns();
			for (int i = 0; i < cols.size(); i++) {
				Object val = values.get(i);
				if ( val != null ) {
					String col = cols.get(i).getName();
					insert.addValue(Factory.fieldByName(col), val);
				}
			}
			return insert;
		}
		
		private org.jooq.Table<Record> getJooqTable(RelationalSchema schema, Table<?> table) {
			if ( isSchemaLess() ) {
				return Factory.tableByName(table.getName());
			} else {
				return Factory.tableByName(schema.getName(), table.getName());
			}
		}
		
		private boolean isSchemaLess() {
			return create.getDialect() == SQLDialect.SQLITE;
		}

	}
}
