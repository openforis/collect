package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.Statement;

import org.jooq.ConnectionProvider;
import org.jooq.impl.DialectAwareJooqConfiguration;
import org.openforis.collect.persistence.jooq.CollectDSLContext;

public class DbInitializer {
	
	private ConnectionProvider connectionProvider;

	public DbInitializer(ConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
	}
	
	public void start() {
		DialectAwareJooqConfiguration jooqConf = new DialectAwareJooqConfiguration(connectionProvider);
		CollectDSLContext dslContext = new CollectDSLContext(jooqConf);
		if (! dslContext.isSchemaLess()) {
			createDbSchema();
		}
	}
	
	private void createDbSchema() {
		Connection conn = null;
		try {
			conn = connectionProvider.acquire();
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();
			stmt.execute(String.format("CREATE SCHEMA IF NOT EXISTS %s", DbUtils.SCHEMA_NAME));
			conn.commit();
		} catch (Exception e) {
			throw new RuntimeException("Error creating schema", e);
		} finally {
			if (conn != null) {
				connectionProvider.release(conn);
			}
		}
	}
	

}