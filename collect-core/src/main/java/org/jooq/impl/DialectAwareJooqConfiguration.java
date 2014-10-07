package org.jooq.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;

public class DialectAwareJooqConfiguration extends DefaultConfiguration {

	private static final long serialVersionUID = 1L;
	
	private enum Database {
		POSTGRES("PostgreSQL", SQLDialect.POSTGRES),
		DERBY("Apache Derby", SQLDialect.DERBY),
		SQLITE("SQLite", SQLDialect.SQLITE),
		SQLITE_FOR_ANDROID("SQLite for Android", SQLDialect.SQLITE),
        H2("H2", SQLDialect.H2);
		
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
		super(createConfiguration(connection));
	}

	private static SQLDialect getDialect(Connection conn) {
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			String dbName = metaData.getDatabaseProductName();
			Database db = Database.getByProductName(dbName);
			if ( db == null ) {
				throw new IllegalArgumentException("Unknown database "+dbName);
			}
			return db.getDialect();
		} catch (SQLException e) {
			throw new RuntimeException("Error getting database name", e);
		}
	}
	
	private static Configuration createConfiguration(Connection connection) {
		return createConfiguration(getDialect(connection));
	}
	
	private static Configuration createConfiguration(SQLDialect dialect) {
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
		configuration.set(settings);
		configuration.set(dialect);
		return configuration;
	}
	
	
	
}
