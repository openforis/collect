package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DbUtils {
	
	public static final String DB_JNDI_RESOURCE_NAME = "jdbc/collectDs";
	public static final String SCHEMA_NAME = "collect";
	
	private static final Logger LOG = LogManager.getLogger(DbUtils.class);
	
	public static Connection getConnection() {
		try {
			DataSource datasource = getDataSource();
			return datasource.getConnection();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static DataSource getDataSource() {
		try {
			DataSource ds;
			Context initCtx = new InitialContext();
			try {
				ds = lookupDataSource(initCtx);
				LOG.info("Data source found in initial context with name " + DB_JNDI_RESOURCE_NAME);
			} catch (NamingException e) {
				//try to look for data source in Environment Context
				Context envCtx = (Context) initCtx.lookup("java:comp/env");
				ds = lookupDataSource(envCtx);
				LOG.info("Data source found in environment context with name " + "java:comp/env" + DB_JNDI_RESOURCE_NAME);
			}
			return ds;
		} catch (Exception e) {
			throw new RuntimeException("", e);
		}
	}

	private static DataSource lookupDataSource(Context ctx) throws NamingException {
		return (DataSource) ctx.lookup(DbUtils.DB_JNDI_RESOURCE_NAME);
	}

	public static void closeQuietly(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {}
		}
	}
}