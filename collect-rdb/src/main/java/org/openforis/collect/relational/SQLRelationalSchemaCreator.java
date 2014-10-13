package org.openforis.collect.relational;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.PrimaryKeyColumn;
import org.openforis.collect.relational.model.PrimaryKeyConstraint;
import org.openforis.collect.relational.model.ReferentialConstraint;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;
import org.openforis.collect.relational.model.UniquenessConstraint;

/**
 * 
 * @author S. Ricci
 *
 */
public class SQLRelationalSchemaCreator implements RelationalSchemaCreator, RelationalSchemaWriter {

	@Override
	public void createRelationalSchema(RelationalSchema schema, Connection conn) throws CollectRdbException {
		boolean schemaless = false;
		Writer writer = null;
		try {
			File file = File.createTempFile("rdb_" + schema.getName(), ".sql");
			writer = new FileWriter(file);
			writeSchema(writer, schema, schemaless);
		} catch(IOException e) {
			
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	@Override
	public void writeRelationalSchema(Writer writer, RelationalSchema schema) throws IOException {
		writeSchema(writer, schema, false);
	}
	
	@Override
	public void writeRelationalSchema(Writer writer, RelationalSchema schema, String databaseProductName) throws IOException {
		boolean schemaless = "SQLite".equals(databaseProductName);
		writeSchema(writer, schema, schemaless);
	}
	
	private void writeSchema(Writer writer, RelationalSchema schema, boolean schemaless) throws IOException {
		for (Table<?> table : schema.getTables()) {
			writeTable(writer, schema, schemaless, table);
		}
		writeForeignKeys(writer, schema, schemaless);
	}

	private void writeTable(Writer writer, RelationalSchema schema, boolean schemaless, Table<?> table) throws IOException {
		writer.write("CREATE TABLE ");
		if ( ! schemaless ) {
			writer.write(schema.getName());
			writer.write('.');
		}
		writer.write(table.getName());
		writer.write(" (");
		writer.write('\n');
		List<Column<?>> columns = table.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			Column<?> column = columns.get(i);
			writeColumn(writer, column);
			if ( i < columns.size() - 1 ) {
				writer.write(',');
			}
			writer.write('\n');
		}
		writer.write(");");
		writer.write('\n');
	}

	private void writeColumn(Writer writer, Column<?> column)
			throws IOException {
		writer.write('\t');
		writer.write(column.getName());
		writer.write(' ');
		writer.write(column.getTypeName());
		if ( column.getLength() != null ) {
			writer.write('(');
			writer.write(column.getLength().toString());
			writer.write(')');
		}
		if ( ! column.isNullable() ) {
			writer.write(" NOT NULL");
		}
		if ( column instanceof PrimaryKeyColumn ) {
			writer.write(" PRIMARY KEY");
		}
	}

	private void writeForeignKeys(Writer writer, RelationalSchema schema, boolean schemaless) throws IOException {
		for (Table<?> table : schema.getTables()) {
			List<ReferentialConstraint> fks = table.getReferentialContraints();
			if ( ! fks.isEmpty() ) {
				writer.write("ALTER TABLE ");
				if ( ! schemaless ) {
					writer.write(schema.getName());
					writer.write('.');
				}
				writer.write(table.getName());
				writer.write('\n');
				for (int i = 0; i < fks.size(); i++) {
					ReferentialConstraint fk = fks.get(i);
					writer.write('\t');
					writeAddForeignKeyConstraint(writer, fk);
					if ( i < fks.size() - 1 ) {
						writer.write(", \n");
					} else {
						writer.write(";");
					}
				}
			}
			writer.write('\n');
		}
	}

	private void writeAddForeignKeyConstraint(Writer w, ReferentialConstraint fk) throws IOException {
		w.write(" ADD CONSTRAINT ");
		w.write(fk.getName());
		w.write(" FOREIGN KEY ");
		w.write('(');
		writeColumnNameSet(w, fk.getColumns());
		w.write(')');
		w.write(" REFERENCES ");
		Table<?> referencedTable = fk.getTable();
		UniquenessConstraint referencedKey = fk.getReferencedKey();
		w.write(referencedKey.getTable().getName());
		PrimaryKeyConstraint referencedTablePK = referencedTable.getPrimaryKeyConstraint();
		w.write('(');
		writeColumnNameSet(w, referencedTablePK.getColumns());
		w.write(")");
	}

	private void writeColumnNameSet(Writer w, List<Column<?>> referencedColumns) throws IOException {
		for (int i = 0; i < referencedColumns.size(); i++) {
			if ( i > 0 ) {
				w.write(',');
			}
			w.write(referencedColumns.get(i).getName());
		}
	}

}
