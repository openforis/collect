package org.openforis.collect.persistence.jooq;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class JooqDaoSupport extends JdbcDaoSupport {
	private final Log log = LogFactory.getLog(getClass());

	protected Log getLog() {
		return log;
	}
	
	protected DialectAwareJooqFactory getJooqFactory() {
		Connection connection = getConnection();
		return new DialectAwareJooqFactory(connection);
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
