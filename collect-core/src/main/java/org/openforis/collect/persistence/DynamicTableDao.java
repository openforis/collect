/**
 * 
 */
package org.openforis.collect.persistence;

import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.Lookup;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
@Transactional
public class DynamicTableDao extends JooqDaoSupport {

	@Transactional
	public Object load(String table, String column, Object... keys) {
		if (keys.length % 2 == 1) {
			throw new IllegalArgumentException("Invalid columns " + keys);
		}
		DialectAwareJooqFactory factory = getJooqFactory();
		Lookup lookupTable = Lookup.getInstance(table);
		SelectJoinStep select = factory.select(lookupTable.getFieldByName(column)).from(lookupTable);
		for (int i = 0; i < keys.length;) {
			String colName = keys[i++].toString();
			String colValue = keys[i++].toString();
			select.where(lookupTable.getFieldByName(colName).equal(colValue));
		}
		Record record = select.fetchOne();
		if (record != null) {
			String field = record.getValueAsString(column);
			return field;
		}
		return null;
	}

}
