package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.ConnectionProvider;
import org.jooq.impl.DialectAwareJooqConfiguration;
import org.openforis.collect.persistence.jooq.CollectDSLContext;

public class DbInitializer {
	
	private static final Log LOG = LogFactory.getLog(DbInitializer.class);
	
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
			LOG.info("Acquiring connection...");
			
			conn = connectionProvider.acquire();
			conn.setAutoCommit(false);
			
			LOG.info("Connection acquired!");
			LOG.info(String.format("Creating schema %s if not exists...", DbUtils.SCHEMA_NAME));

			if (createSchemaIfNotExists(conn)) {
				createSchema(conn);
			}
			LOG.info(String.format("Schema '%s' created (if not existing already)", DbUtils.SCHEMA_NAME));
		} catch (Exception e) {
			
		} finally {
			if (conn != null) {
				connectionProvider.release(conn);
			}
		}
	}

	private void createSchema(Connection conn) {
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(String.format("CREATE SCHEMA '%s'", DbUtils.SCHEMA_NAME));
			conn.commit();
		} catch(Exception e) {
			String errorMessage = String.format("Error creating schema '%s'", DbUtils.SCHEMA_NAME);
			LOG.error(errorMessage, e);
			throw new RuntimeException(errorMessage, e);
		}
	}

	private boolean createSchemaIfNotExists(Connection conn) {
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(String.format("CREATE SCHEMA IF NOT EXISTS '%s'", DbUtils.SCHEMA_NAME));
			conn.commit();
			return true;
		} catch(Exception e) {
			//create schema if not exists not supported
			return false;
		}
	}
}