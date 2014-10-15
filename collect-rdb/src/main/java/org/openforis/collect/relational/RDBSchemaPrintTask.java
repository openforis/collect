package org.openforis.collect.relational;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.relational.data.RDBPrintTask;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.PrimaryKeyColumn;
import org.openforis.collect.relational.model.PrimaryKeyConstraint;
import org.openforis.collect.relational.model.ReferentialConstraint;
import org.openforis.collect.relational.model.Table;
import org.openforis.collect.relational.model.UniquenessConstraint;

/**
 * 
 * @author S. Ricci
 *
 */
public class RDBSchemaPrintTask extends RDBPrintTask {
	
	@Override
	protected void execute() throws Throwable {
		if ( ! isSchemaless() ) {
			writer.write("CREATE SCHEMA ");
			writer.write(schema.getName());
			writer.write(';');
			writer.write('\n');
		}
		for (Table<?> table : schema.getTables()) {
			writeTable(table);
		}
		writeForeignKeys();
	}

	private void writeTable(Table<?> table) throws IOException {
		writer.write("CREATE TABLE ");
		if ( ! isSchemaless() ) {
			writer.write(schema.getName());
			writer.write('.');
		}
		writer.write(table.getName());
		writer.write(" (");
		writer.write('\n');
		List<Column<?>> columns = table.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			Column<?> column = columns.get(i);
			writeColumn(column);
			if ( i < columns.size() - 1 ) {
				writer.write(',');
			}
			writer.write('\n');
		}
		writer.write(");");
		writer.write('\n');
	}

	private void writeColumn(Column<?> column) throws IOException {
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

	private void writeForeignKeys() throws IOException {
		for (Table<?> table : schema.getTables()) {
			List<ReferentialConstraint> fks = table.getReferentialContraints();
			if ( ! fks.isEmpty() ) {
				writer.write("ALTER TABLE ");
				writer.write(getQualifiedTableName(table));
				writer.write('\n');
				for (int i = 0; i < fks.size(); i++) {
					ReferentialConstraint fk = fks.get(i);
					writer.write('\t');
					writeAddForeignKeyConstraint(fk);
					if ( i < fks.size() - 1 ) {
						writer.write(", \n");
					} else {
						writer.write(";");
					}
				}
				writer.write('\n');
			}
		}
	}

	private void writeAddForeignKeyConstraint(ReferentialConstraint fk) throws IOException {
		writer.write(" ADD CONSTRAINT ");
		writer.write(fk.getName());
		writer.write(" FOREIGN KEY ");
		writer.write('(');
		writeColumnNameSet(fk.getColumns());
		writer.write(')');
		writer.write(" REFERENCES ");
		UniquenessConstraint referencedKey = fk.getReferencedKey();
		Table<?> referencedTable = referencedKey.getTable();
		writer.write(getQualifiedTableName(referencedTable));
		PrimaryKeyConstraint referencedTablePK = referencedTable.getPrimaryKeyConstraint();
		writer.write('(');
		writeColumnNameSet(referencedTablePK.getColumns());
		writer.write(")");
	}

	private void writeColumnNameSet(List<Column<?>> referencedColumns) throws IOException {
		for (int i = 0; i < referencedColumns.size(); i++) {
			if ( i > 0 ) {
				writer.write(',');
			}
			writer.write(referencedColumns.get(i).getName());
		}
	}

	private String getQualifiedTableName(Table<?> table) {
		StringBuffer sb = new StringBuffer();
		if( ! isSchemaless() ) {
			sb.append(schema.getName());
			sb.append('.');
		}
		sb.append(table.getName());
		return sb.toString();
	}

}