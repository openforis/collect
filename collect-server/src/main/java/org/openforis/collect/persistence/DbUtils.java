package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
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
			Context initCtx = new InitialContext();
			DataSource ds;
			try {
				ds = (DataSource) initCtx.lookup(DbUtils.DB_JNDI_RESOURCE_NAME);
			} catch (Exception e) {
				//try to prepend environment prefix
				ds = (DataSource) initCtx.lookup("java:comp/env/" + DbUtils.DB_JNDI_RESOURCE_NAME);
			}
			return ds;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void closeQuietly(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {}
		}
	}
}