/**
 * 
 */
package org.openforis.collect.persistence;

import org.jooq.Field;
import org.jooq.SimpleSelectWhereStep;
import org.jooq.impl.TableImpl;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.idm.model.Coordinate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 *
 */
public class ExternalLookupProviderDAO extends JooqDaoSupport {

	@Transactional
	public Coordinate load(String table, String column, Object... keys){
		DialectAwareJooqFactory jooqFactory = getJooqFactory();
		//jooqFactory.select(new TableFieldImpl)
		//SimpleSelectWhereStep<?> select = jooqFactory.select From(new TableImpl(table));
		return null;
	}
	
}
