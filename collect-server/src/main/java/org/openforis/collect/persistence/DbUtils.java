package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public abstract class DbUtils {
	
	public static final String DB_JNDI_RESOURCE_NAME = "java:comp/env/jdbc/collectDs";
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
			Context initialContext = new InitialContext();
			DataSource datasource = (DataSource) initialContext.lookup(DbUtils.DB_JNDI_RESOURCE_NAME);
			return datasource;
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