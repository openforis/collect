/**
 * 
 */
package org.openforis.collect.persistence;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.TableField;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.Lookup;
import org.openforis.collect.persistence.jooq.tables.records.LookupRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
@Transactional
public class DynamicTableDao extends JooqDaoSupport {

	private static final String SURVEY_ID_FIELD = "survey_id";
	private static final String SURVEY_WORK_ID_FIELD = "survey_work_id";

	@Transactional
	public Object load(String table, String column, Object... keys) {
		return load(null, false, table, column, keys);
	}
	
	@Transactional
	public Object load(Integer surveyId, boolean work, String table, String column, Object... keys) {
		if (keys.length % 2 == 1) {
			throw new IllegalArgumentException("Invalid columns " + keys);
		}
		DialectAwareJooqFactory factory = getJooqFactory();
		Lookup lookupTable = Lookup.getInstance(table);
		SelectJoinStep select = factory.select(lookupTable.getFieldByName(column)).from(lookupTable);
		
		if ( surveyId != null ) {
			String surveyIdFieldName = work ? SURVEY_WORK_ID_FIELD : SURVEY_ID_FIELD;
			TableField<LookupRecord, Integer> surveyIdTableField = lookupTable.createIntegerField(surveyIdFieldName);
			select.where(surveyIdTableField.equal(surveyId));
		}
		for (int i = 0; i < keys.length;) {
			String colName = keys[i++].toString();
			String colValue = keys[i++].toString();
			TableField<LookupRecord, String> tableField = lookupTable.getFieldByName(colName);
			Condition condition = tableField.equal(colValue);
			if ( StringUtils.isBlank(colValue) ) {
				condition = condition.or(tableField.isNull());
			}
			select.where(condition);
		}
		Record record = select.fetchOne();
		if (record != null) {
			String field = record.getValueAsString(column);
			return field;
		}
		return null;
	}

}
