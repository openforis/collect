package org.openforis.collect.persistence.jooq;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.DSLContext;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DialectAwareJooqConfiguration;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class JooqDaoSupport extends JdbcDaoSupport {
	private final Log log = LogFactory.getLog(getClass());

	protected Log getLog() {
		return log;
	}
	
	protected DSLContext dsl() {
		Connection connection = getConnection();
		return new DefaultDSLContext(new DialectAwareJooqConfiguration(connection));
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
