/**
 * 
 */
package org.openforis.collect.persistence;

import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.Lookup;
import org.openforis.idm.model.Coordinate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
@Transactional
public class LookupProviderDao extends JooqDaoSupport {

	@Transactional
	public Coordinate load(String table, String column, Object... keys) {
		DialectAwareJooqFactory factory = getJooqFactory();
		Lookup lookupTable = Lookup.getInstance(table);
		SelectJoinStep select = factory.select(lookupTable.getFieldByName(column)).from(lookupTable);
		int i = 0;
		for (Object object : keys) {
			String keyColumn = "key" + (++i);
			select.where(lookupTable.getFieldByName(keyColumn).equal(object.toString()));
		}
		Record record = select.fetchOne();
		if (record != null) {
			String field = record.getValueAsString(column);
			Coordinate coordinate = Coordinate.parseCoordinate(field);
			return coordinate;
		}
		return null;
	}

}
