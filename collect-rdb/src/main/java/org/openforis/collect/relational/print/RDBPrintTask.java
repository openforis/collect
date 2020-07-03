package org.openforis.collect.relational.print;

import static org.openforis.collect.relational.util.SQLUtils.doubleQuote;
import static org.openforis.collect.relational.util.SQLUtils.quote;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.relational.data.DataExtractor;
import org.openforis.collect.relational.data.Row;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;
import org.openforis.collect.relational.print.RDBPrintJob.RdbDialect;
import org.openforis.concurrency.Task;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class RDBPrintTask extends Task {
	
	private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	//input
	protected RelationalSchema schema;
	protected Writer writer;
	protected RdbDialect dialect;
	private String dateTimeFormat;
	
	//transient
	private transient SimpleDateFormat dateFormatter;
	
	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		if (StringUtils.isBlank(this.dateTimeFormat)) {
			this.dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;
		}
		this.dateFormatter = new SimpleDateFormat(dateTimeFormat);
	}
	
	protected void writeBatchInsert(Table<?> table, DataExtractor extractor) throws IOException {
		if(extractor.hasNext()) {
			writer.write("INSERT INTO ");
			writer.write(getQualifiedName(table));
			writer.write('(');
			writer.write(StringUtils.join(doubleQuote(getColumnNames(table)), ", "));
			writer.write(')');
			writer.write(" VALUES ");
			writer.write('\n');
			while(extractor.hasNext()) {
				if(!isRunning()) {
					return;
				}
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
			writer.write('\n');
		}
	}

	private List<String> getStringValues(Row row) {
		List<Object> values = row.getValues();
		List<String> stringValues = new ArrayList<String>();
		for (Object val : values) {
			stringValues.add(getStringValue(val));
		}
		return stringValues;
	}

	private String getStringValue(Object val) {
		if ( val == null ) {
			return "null";
		} else if ( val instanceof String ) {
			String escapedVal = StringUtils.replace((String) val, "'", "''");
			return quote(escapedVal);
		} else if ( val instanceof Date ) {
			return quote(dateFormatter.format(val));
		} else {
			return val.toString();
		}
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
		if ( ! isSchemaless() ) {
			sb.append(doubleQuote(schema.getName()));
			sb.append('.');
		}
		sb.append(doubleQuote(table.getName()));
		return sb.toString();
	}
	
	protected boolean isSchemaless() {
		return dialect == RdbDialect.SQLITE;
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}
	
	public void setSchema(RelationalSchema schema) {
		this.schema = schema;
	}
	
	public void setDialect(RdbDialect dialect) {
		this.dialect = dialect;
	}
	
	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}
}
