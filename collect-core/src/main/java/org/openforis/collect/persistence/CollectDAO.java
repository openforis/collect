package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
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
	private static final String POSTGRESQL_DBNAME = "PostgreSQL";
	private static final String APACHE_DERBY_DBNAME = "Apache Derby";
	private final Log log = LogFactory.getLog(getClass());

	protected Factory getJooqFactory() {
		Connection conn = getConnection();
		SQLDialect dialect = getDialect(conn);
		Factory jooqFactory = new Factory(conn, dialect);
		return jooqFactory; 
	}

	private SQLDialect getDialect(Connection conn) {
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			String dbName = metaData.getDatabaseProductName();
			if ( dbName.equals(APACHE_DERBY_DBNAME) ) {
				return SQLDialect.DERBY;
			} else if ( dbName.equals(POSTGRESQL_DBNAME) ) {
				return SQLDialect.POSTGRES;
			} else {
				throw new IllegalArgumentException("Unknown database "+dbName);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error getting database name", e);
		}
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
