package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.SQLDialect;
import org.jooq.impl.Factory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class CollectDAO extends JdbcDaoSupport {
	private final Log log = LogFactory.getLog(getClass());

	protected Factory getJooqFactory() {
		Connection conn = getConnection();
		Factory jooqFactory = new Factory(conn, SQLDialect.POSTGRES);
		return jooqFactory; 
	}

	protected Log getLog() {
		return log;
	}
	
	protected static Timestamp toTimestamp(Date date) {
		if ( date == null ) {
			return null;
		} else {
			return new Timestamp(date.getTime());
		}
	}
}
