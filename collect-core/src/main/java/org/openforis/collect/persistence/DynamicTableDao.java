/**
 * 
 */
package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectJoinStep;
import org.jooq.TableField;
import org.openforis.collect.model.NameValueEntry;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.Lookup;
import org.openforis.collect.persistence.jooq.tables.records.LookupRecord;
import org.openforis.collect.persistence.utils.TableMetaData;
import org.openforis.collect.persistence.utils.TableMetaData.ColumnMetaData;
import org.openforis.commons.collection.Visitor;

/**
 * @author M. Togna
 * 
 */
public class DynamicTableDao extends JooqDaoSupport {

	@Deprecated
	public Object load(String table, String column, Object... keys) {
		NameValueEntry[] filters = NameValueEntry.fromKeyValuePairs(keys);
		return loadValue(table, column, filters);
	}

	public Object loadValue(String table, String column, NameValueEntry... filters) {
		Map<String, String> row = loadRow(table, filters);
		return row == null ? null: row.get(column);
	}

	public Map<String, String> loadRow(String table, NameValueEntry[] filters) {
		List<Map<String, String>> rows = loadRows(table, filters);
		if ( rows == null || rows.isEmpty()) {
			return null;
		} else {
			return rows.get(0);
		}
	}

	public List<Map<String, String>> loadRows(String table, NameValueEntry[] filters) {
		return loadRows(table, filters, (String[]) null);
	}
	
	public List<Map<String, String>> loadRows(String table, NameValueEntry[] filters, String[] notNullColumns) {
		final List<Map<String, String>> result = new ArrayList<Map<String,String>>();
		visitRows(table, filters, notNullColumns, new Visitor<Map<String,String>>() {
			public void visit(Map<String, String> item) {
				result.add(item);
			}
		});
		return result;
	}
	
	public void visitRows(String table, NameValueEntry[] filters, String[] notNullColumns, Visitor<Map<String, String>> visitor) {
		Lookup lookupTable = getLookupTable(table);
		CollectDSLContext dsl = dsl();
		Field<?>[] fields = lookupTable.fields();
		SelectJoinStep<Record> select = dsl.select(fields).from(lookupTable);
		
		addFilterConditions(lookupTable, select, filters);
		addNotNullConditions(lookupTable, select, notNullColumns);
		
		Cursor<Record> cursor = null;
		try {
			cursor = select.fetchLazy();
			while (cursor.hasNext()) {
				Record r = cursor.fetchOne();
				Map<String, String> rowMap = parseRecord(r, fields);
				visitor.visit(rowMap);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
		    }
		}
	}

	public boolean exists(String table, NameValueEntry[] filters, String[] notNullColumns) {
		Lookup lookupTable = getLookupTable(table);
		SelectJoinStep<Record1<Integer>> select = dsl().selectCount().from(lookupTable);
		addFilterConditions(lookupTable, select, filters);
		addNotNullConditions(lookupTable, select, notNullColumns);
		Record record = select.fetchOne();
		Integer count = (Integer) record.getValue(0);
		return count > 0;
	}

	private Lookup getLookupTable(String table) {
		Lookup lookupTable = Lookup.getInstance(table);
		if (! lookupTable.isInitialized()) {
			initializeTable(lookupTable);
		}
		return lookupTable;
	}
	
	protected void addFilterConditions(Lookup lookupTable,
			SelectJoinStep<? extends Record> select, NameValueEntry[] filters) {
		for (NameValueEntry filter : filters) {
			String colName = filter.getKey();
			@SuppressWarnings("unchecked")
			TableField<LookupRecord, Object> tableField = (TableField<LookupRecord, Object>) lookupTable.field(colName);
			if ( tableField != null ) {
				Object filterValue = filter.getValue();
				Condition condition;
				if ( (tableField.getType().equals(String.class) ) && 
						(filterValue == null || filterValue instanceof String && StringUtils.isEmpty((String) filterValue)) ) {
					condition = tableField.isNull().or(tableField.trim().equal(""));
				} else if ( filterValue == null ) {
					condition = tableField.isNull();
				} else {
					condition = tableField.equal(filterValue);
				}
				select.where(condition);
			} else {
				log.warn("Filter not applied: " + filter);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void addNotNullConditions(Lookup lookupTable, SelectJoinStep<? extends Record> select, String[] columns) {
		if ( columns != null ) {
			for (String colName : columns) {
				Field<?> tableField = lookupTable.field(colName);
				if ( tableField != null ) {
					select.where(tableField.isNotNull().and(((Field<String>) tableField).notEqual("")));
				} else {
					log.warn("Not null filter not applied on column: " + colName);
				}
			}
		}
	}

	private void initializeTable(Lookup table) {
		TableMetaData tableMetadata = loadTableMetadata(table);
		table.initialize(tableMetadata);
	}

	private TableMetaData loadTableMetadata(Lookup table) {
		try {
			CollectDSLContext dsl = dsl();
			Connection connection = dsl.configuration().connectionProvider().acquire();
			DatabaseMetaData metaData = connection.getMetaData();
			String schemaName = table.getSchema().getName();
			String tableName = table.getName();
			return extractTableMetaData(metaData, schemaName, tableName);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private TableMetaData extractTableMetaData(DatabaseMetaData metaData, String schemaName,
			String tableName) throws SQLException {
		ResultSet columnRs = metaData.getColumns(null, schemaName, tableName, null);
		boolean metaDataFound = columnRs.next();
		if (! metaDataFound) {
			columnRs = metaData.getColumns(null, schemaName.toUpperCase(Locale.ENGLISH), tableName.toUpperCase(Locale.ENGLISH), null);
			metaDataFound = columnRs.next();
		}
		if (metaDataFound) {
			TableMetaData tableMetaData = new TableMetaData();
			do {
				String colName = columnRs.getString("COLUMN_NAME").toLowerCase(Locale.ENGLISH);
				int dataType = columnRs.getInt("DATA_TYPE");
				tableMetaData.addColumnMetaData(new ColumnMetaData(colName, dataType));
			} while (columnRs.next());
			return tableMetaData;
		} else {
			throw new IllegalStateException(String.format("Could not extract metadata for table %s.%s", schemaName, tableName));
		}
	}

	protected Map<String, String> parseRecord(Record record, Field<?>[] fields) {
		Map<String, String> rowMap = new HashMap<String, String>();
		for (Field<?> field : fields) {
			String key = field.getName();
			Object val = record.getValue(field);
			rowMap.put(key, val == null ? null: val.toString());
		}
		return rowMap;
	}
	
}
