package org.openforis.collect.persistence.jooq;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.jooq.SQLDialect;
import org.jooq.impl.Factory;

/**
 * @author G. Miceli
 */
public class DialectAwareJooqFactory extends Factory {
	
	private static final long serialVersionUID = 1L;
	
	private enum Database {
		POSTGRES("PostgreSQL", SQLDialect.POSTGRES),
		DERBY("Apache Derby", SQLDialect.DERBY),
		SQLITE("SQLite", SQLDialect.SQLITE);
		
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
	
	public DialectAwareJooqFactory(Connection connection) {
		super(connection, getDialect(connection));
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



}
