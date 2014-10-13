package org.openforis.collect.relational;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import liquibase.util.StringUtils;

import org.openforis.collect.model.CollectRecord;
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
 * @author S. Ricci
 *
 */
public class SQLDataExporter {
	
	private RelationalSchema schema;
	private boolean schemaless;
	
	public SQLDataExporter(RelationalSchema schema, String databaseProductName) {
		this.schema = schema;
		this.schemaless = "SQLite".equals(databaseProductName);
	}	

	public void writeReferenceDataSQLInserts(Writer writer) throws CollectRdbException, IOException {
		for (CodeTable codeTable : schema.getCodeListTables()) {
			CodeTableDataExtractor extractor = new CodeTableDataExtractor(codeTable);
			writeBatchInsert(writer, codeTable, extractor);
		}
	}
	
	public void writeDataSQLInserts(Writer writer, CollectRecord record) throws CollectRdbException, IOException {
		for (DataTable table : schema.getDataTables()) {
			DataTableDataExtractor extractor = new DataTableDataExtractor(table, record);
			writeBatchInsert(writer, table, extractor);
		}
	}

	private void writeBatchInsert(Writer writer, Table<?> table, DataExtractor extractor) throws IOException {
		writer.write("INSERT INTO ");
		writer.write(getQualifiedName(table));
		writer.write('(');
		writer.write(StringUtils.join(getColumnNames(table), ", "));
		writer.write(')');
		writer.write(" VALUES ");
		while(extractor.hasNext()) {
			Row row = extractor.next();
			writer.write('\t');
			writer.write('(');
			List<String> stringValues = getStringValues(row);
			writer.write(StringUtils.join(stringValues, ", "));
			writer.write(')');
			if ( extractor.hasNext() ) {
				writer.write(',');
				writer.write('\n');
			}
		}
		writer.write(';');
	}

	private List<String> getStringValues(Row row) {
		List<Object> values = row.getValues();
		List<String> stringValues = new ArrayList<String>();
		for (Object val : values) {
			if ( val == null ) {
				stringValues.add("null");
			} else if ( val instanceof String ) {
				stringValues.add("'" + val + "'");
			} else {
				stringValues.add(val.toString());
			}
		}
		return stringValues;
	}

	private List<String> getColumnNames(Table<?> table) {
		List<Column<?>> columns = table.getColumns();
		List<String> names = new ArrayList<String>();
		for (Column<?> column : columns) {
			names.add(column.getName());
		}
		return names;
	}

	private String getQualifiedName(Table<?> table) {
		StringBuilder sb = new StringBuilder();
		if ( ! schemaless ) {
			sb.append(schema.getName());
			sb.append('.');
		}
		sb.append(table.getName());
		return sb.toString();
	}
	
}
