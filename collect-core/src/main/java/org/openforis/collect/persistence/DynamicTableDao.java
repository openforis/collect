/**
 * 
 */
package org.openforis.collect.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
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
		return load(table, column, filtersArray);
	}
	
	@Transactional
	public Object load(String table, String column, StringKeyValuePair... filters) {
		Map<String, String> row = loadRow(table, filters);
		return row.get(column);
	}

	@Transactional
	public Map<String, String> loadRow(String table, StringKeyValuePair[] filters) {
		DialectAwareJooqFactory factory = getJooqFactory();
		Lookup lookupTable = Lookup.getInstance(table);
		List<Field<?>> fields = lookupTable.getFields();
		SelectJoinStep select = factory.select(fields).from(lookupTable);
		
		for (StringKeyValuePair filter : filters) {
			String colName = filter.getKey();
			String colValue = filter.getValue();
			TableField<LookupRecord, String> tableField = lookupTable.getFieldByName(colName);
			Condition condition = tableField.equal(colValue);
			if ( StringUtils.isBlank(colValue) ) {
				condition = condition.or(tableField.isNull());
			}
			select.where(condition);
		}
		Map<String, String> result = new HashMap<String, String>();
		Record record = select.fetchOne();
		if (record != null) {
			for (Field<?> field : fields) {
				String key = field.getName();
				String value = record.getValueAsString(key);
				result.put(key, value);
			}
		}
		return CollectionUtils.unmodifiableMap(result);
	}

	@Transactional
	public Map<String, String> loadRows(Integer surveyId, boolean work,
			String table, StringKeyValuePair[] filters) {
	}
	
}
