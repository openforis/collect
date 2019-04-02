package org.jooq.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;

/**
 * 
 * @author S. Ricci
 *
 */
public class DialectAwareJooqConfiguration extends DefaultConfiguration {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogManager.getLogger(DialectAwareJooqConfiguration.class);
	
	private enum Database {
		POSTGRES("PostgreSQL", SQLDialect.POSTGRES),
		DERBY("Apache Derby", SQLDialect.DERBY),
		SQLITE("SQLite", SQLDialect.SQLITE),
		SQLITE_FOR_ANDROID("SQLite for Android", SQLDialect.SQLITE),
        H2("H2", SQLDialect.H2),
        MYSQL("MySQL", SQLDialect.MYSQL),
        MS_SQL_SERVER("Microsoft SQL Server", SQLDialect.DEFAULT);
		
		private String productName;
		private SQLDialect dialect;

		Database(String productName, SQLDialect dialect) {
			this.productName = productName;
			this.dialect = dialect;
		}
		
		public static Database getByProductName(String name) {
			for (Database db : Database.values()) {
				if ( name.equals(db.getProductName())) {
					return db;
				}
			}
			return null;
		}
		
		public String getProductName() {
			return productName;
		}
		
		public SQLDialect getDialect() {
			return dialect;
		}
	}
	
	public DialectAwareJooqConfiguration(Connection connection) {
		this(new DefaultConnectionProvider(connection));
	}

	public DialectAwareJooqConfiguration(ConnectionProvider connectionProvider) {
		super(createConfiguration(connectionProvider));
	}
	
	private static Configuration createConfiguration(ConnectionProvider connectionProvider) {
		SQLDialect dialect = getDialect(connectionProvider);
		Settings settings = new Settings();
		switch ( dialect ) {
		case SQLITE:
			settings.withRenderSchema(false);
			break;
		case H2:
		    settings.setRenderNameStyle(RenderNameStyle.AS_IS);
		    break;
		default:
		}
		DefaultConfiguration configuration = new DefaultConfiguration();
		configuration.setConnectionProvider(connectionProvider);
		configuration.setSettings(settings);
		configuration.setSQLDialect(dialect);
		return configuration;
	}

	private static SQLDialect getDialect(ConnectionProvider connectionProvider) {
		Connection conn = null;
		try {
			conn = connectionProvider.acquire();
			DatabaseMetaData metaData = conn.getMetaData();
			String dbName = metaData.getDatabaseProductName();
			Database db = Database.getByProductName(dbName);
			if ( db == null ) {
				LOG.warn("Unknown database type: " + dbName);
				return SQLDialect.DEFAULT;
			} else {
				return db.getDialect();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error getting database name", e);
		} finally {
			connectionProvider.release(conn);
		}
	}
	
}
