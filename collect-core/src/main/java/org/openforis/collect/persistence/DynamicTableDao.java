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

import liquibase.statement.NotNullConstraint;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectJoinStep;
import org.jooq.TableField;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.Lookup;
import org.openforis.collect.persistence.jooq.tables.records.LookupRecord;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.StringKeyValuePair;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
@Transactional
public class DynamicTableDao extends JooqDaoSupport {

	@Deprecated
	@Transactional
	public Object load(String table, String column, Object... keys) {
		if ( keys == null || keys.length % 2 == 1 ) {
			throw new IllegalArgumentException("Invalid keys specified: odd couple of values expected");
		}
		List<StringKeyValuePair> filters = new ArrayList<StringKeyValuePair>();
		for(int i = 0; i < keys.length; i ++ ) {
			String key = (String) keys[i];
			String value = (String) keys[i+1];
			StringKeyValuePair pair = new StringKeyValuePair(key, value);
			filters.add(pair);
		}
		StringKeyValuePair[] filtersArray = (StringKeyValuePair[]) filters.toArray(new StringKeyValuePair[] {});
		return loadValue(table, column, filtersArray);
	}
	
	@Transactional
	public Object loadValue(String table, String column, StringKeyValuePair... filters) {
		Map<String, String> row = loadRow(table, filters);
		return row == null ? null: row.get(column);
	}

	@Transactional
	public Map<String, String> loadRow(String table, StringKeyValuePair[] filters) {
		List<Map<String, String>> rows = loadRows(table, filters);
		if ( rows == null || rows.isEmpty()) {
			return null;
		} else {
			return rows.get(0);
		}
	}

	@Transactional
	public List<Map<String, String>> loadRows(String table, StringKeyValuePair[] filters) {
		return loadRows(table, filters, (String[]) null);
	}
	
	@Transactional
	public List<Map<String, String>> loadRows(String table, StringKeyValuePair[] filters, String[] notNullColumns) {
		Lookup lookupTable = Lookup.getInstance(table);
		initTable(table);
		DialectAwareJooqFactory factory = getJooqFactory();
		List<Field<?>> fields = lookupTable.getFields();
		SelectJoinStep select = factory.select(fields).from(lookupTable);
		
		addFilterConditions(lookupTable, select, filters);
		addNotNullConditions(lookupTable, select, notNullColumns);
		
		List<Map<String, String>> result = new ArrayList<Map<String,String>>();
		Result<Record> selectResult = select.fetch();
		if ( selectResult != null ) {
			for (Record record : selectResult) {
				Map<String, String> rowMap = parseRecord(record, fields);
				result.add(rowMap);
			}
		}
		return CollectionUtils.unmodifiableList(result);
	}

	protected void addFilterConditions(Lookup lookupTable,
			SelectJoinStep select, StringKeyValuePair[] filters) {
		for (StringKeyValuePair filter : filters) {
			String colName = filter.getKey();
			@SuppressWarnings("unchecked")
			TableField<LookupRecord, String> tableField = (TableField<LookupRecord, String>) lookupTable.getField(colName);
			if ( tableField != null ) {
				String colValue = filter.getValue();
				Condition condition = tableField.equal(colValue);
				if ( StringUtils.isBlank(colValue) ) {
					condition = condition.or(tableField.isNull());
				}
				select.where(condition);
			} else {
				logger.warn("Filter not applied: " + filter);
			}
		}
	}

	protected void addNotNullConditions(Lookup lookupTable, SelectJoinStep select, String[] columns) {
		if ( columns != null ) {
			for (String colName : columns) {
				@SuppressWarnings("unchecked")
				TableField<LookupRecord, String> tableField = (TableField<LookupRecord, String>) lookupTable.getField(colName);
				if ( tableField != null ) {
					select.where(tableField.isNotNull().and(tableField.notEqual("")));
				} else {
					logger.warn("Not null filter not applied on column: " + colName);
				}
			}
		}
	}

	protected void initTable(String table) {
		Lookup lookupTable = Lookup.getInstance(table);
		Collection<String> colNames = getColumnNames(table);
		for (String colName : colNames) {
			if ( lookupTable.getField(colName) == null ) {
				lookupTable.createFieldByName(colName);
			}
		}
	}
	
	private Collection<String> getColumnNames(String table) {
		List<String> result = new ArrayList<String>();
		try { 
			DialectAwareJooqFactory factory = getJooqFactory();
			Connection connection = factory.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet columns = metaData.getColumns(null, null, table, null);
			while (columns.next()) {
				String colName = columns.getString("COLUMN_NAME");
				result.add(colName);
			}
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	protected Map<String, String> parseRecord(Record record, List<Field<?>> fields) {
		Map<String, String> rowMap = new HashMap<String, String>();
		for (Field<?> field : fields) {
			String key = field.getName();
			String value = record.getValueAsString(key);
			rowMap.put(key, value);
		}
		return rowMap;
	}
	
}
