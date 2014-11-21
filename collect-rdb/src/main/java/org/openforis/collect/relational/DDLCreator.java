package org.openforis.collect.relational;

import java.io.Writer;

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;

public class DDLCreator {
	
	private RelationalSchema schema;

	public DDLCreator(RelationalSchema schema) {
		this.schema = schema;
	}

	public void write(Writer output) {
		Database db = new Database();
		db.setName(schema.getName());
		
		for (Table<?> table : schema.getTables()) {
			org.apache.ddlutils.model.Table ddlTable = new org.apache.ddlutils.model.Table();
			ddlTable.setName(table.getName());
			ddlTable.setSchema(schema.getName());
			
			for (Column<?> column : table.getColumns()) {
				org.apache.ddlutils.model.Column ddlColumn = new org.apache.ddlutils.model.Column();
				ddlColumn.setName(column.getName());
				ddlColumn.setType(column.getTypeName());
				ddlColumn.setTypeCode(column.getType());
				ddlColumn.setSizeAndScale(column.getLength() == null ? 0: column.getLength(), 0);
				ddlTable.addColumn(ddlColumn);
			}
			db.addTable(ddlTable);
		}
		new DatabaseIO().write(db, output);
	}

}
