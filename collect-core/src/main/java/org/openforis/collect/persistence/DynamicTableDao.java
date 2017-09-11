/**
 * 
 */
package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
		Collection<Map<String, ?>> colsMetadata = loadColumnsMetadata(table.getName());
		table.initialize(colsMetadata);
	}

	private List<Map<String, ?>> loadColumnsMetadata(String table) {
		List<Map<String, ?>> result = new ArrayList<Map<String,?>>();
		try {
			CollectDSLContext dsl = dsl();
			Connection connection = dsl.configuration().connectionProvider().acquire();
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet columnRs = metaData.getColumns(null, null, table, null);
			while (columnRs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("COLUMN_NAME", columnRs.getString("COLUMN_NAME"));
				map.put("DATA_TYPE", columnRs.getInt("DATA_TYPE"));
				result.add(map);
			}
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
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
