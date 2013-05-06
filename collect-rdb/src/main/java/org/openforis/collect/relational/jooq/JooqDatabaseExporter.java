package org.openforis.collect.relational.jooq;

import static org.jooq.impl.Factory.fieldByName;
import static org.jooq.impl.Factory.tableByName;

import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.List;

import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.Dataset;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Row;
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
		Dataset dataset = schema.getReferenceData();
		insertDataset(schema, dataset);
	}

	@Override
	public void insertData(RelationalSchema schema, CollectRecord record) throws CollectRdbException  {
		Dataset dataset = schema.createDataset(record);
		insertDataset(schema, dataset);
	}

	private void insertDataset(RelationalSchema schema, Dataset dataset) throws CollectRdbException {
		List<Row> rows = dataset.getRows();
		List<InsertQuery<Record>> inserts = new ArrayList<InsertQuery<Record>>(rows.size());
		try {
			for (int rowno = 0; rowno < rows.size(); rowno++) {
				Row row = rows.get(rowno);
				Table<?> table = row.getTable();
				List<Column<?>> cols = table.getColumns();
				InsertQuery<Record> insert = create.insertQuery(tableByName(schema.getName(), table.getName()));
				List<Object> values = row.getValues();
				for (int colno = 0; colno < cols.size(); colno++) {
					Object val = values.get(colno);
					if ( val != null ) {
						String col = cols.get(colno).getName();
						insert.addValue(fieldByName(col), val);
					}
				}
				inserts.add(insert);
			}
			create.batch(inserts).execute();
		} catch (DataAccessException e) {
			Throwable e2 = e.getCause();
			if ( e2 instanceof BatchUpdateException ) {
				throw new CollectRdbException("Batch insert failed", ((BatchUpdateException) e2).getNextException());
			} else {
				throw new CollectRdbException("Batch insert failed", e);
			}
		}
	}
}
