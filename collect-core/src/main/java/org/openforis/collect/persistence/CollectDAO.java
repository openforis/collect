package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.impl.Factory;
import org.openforis.collect.persistence.jooq.CollectJooqFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class CollectDAO extends JdbcDaoSupport {
	private final Log log = LogFactory.getLog(getClass());

	public Factory getJooqFactory() {
		Connection conn = getConnection();
		Factory jooqFactory = new CollectJooqFactory(conn);
		return jooqFactory; 
	}
	
	protected Log getLog() {
		return log;
	}
	
	// TODO Move to MappingJooqFactory
	protected static Timestamp toTimestamp(Date date) {
		if ( date == null ) {
			return null;
		} else {
			return new Timestamp(date.getTime());
		}
	}
}
