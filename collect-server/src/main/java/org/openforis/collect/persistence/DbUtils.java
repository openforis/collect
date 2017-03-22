package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public abstract class DbUtils {
	
	public static final String DB_JNDI_RESOURCE_NAME = "jdbc/collectDs";
	public static final String SCHEMA_NAME = "collect";
	
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
			} catch (NamingException e) {
				//try to look for data source in Environment Context
				Context envCtx = (Context) initCtx.lookup("java:comp/env");
				ds = lookupDataSource(envCtx);
			}
			return ds;
		} catch (Exception e) {
			throw new RuntimeException(e);
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