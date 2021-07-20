/**
 * 
 */
package org.openforis.collect.persistence.liquibase;

import java.sql.Connection;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.ResourceAccessor;

/**
 * @author S. Ricci
 *
 */
public class DatabaseAwareSpringLiquibase4 extends SpringLiquibase {

	private static final String STANDARD_DIALECT = "standard";
	private static final String DBMS_PLACEHOLDER = "DBMS_ID";

	enum CustomDialectDatabase {
		
		POSTGRESQL("PostgreSQL", "postgresql"),
		SQLITE("SQLite", "sqlite"),
		SQLITE_ANDROID("SQLite for Android", "sqlite");
		
		private String productName;
		private String liquibaseDbms;
		
		CustomDialectDatabase(String productName, String liquibaseDbms) {
			this.productName = productName;
			this.liquibaseDbms = liquibaseDbms;
		}
		
		public String getProductName() {
			return productName;
		}
		
		public String getLiquibaseDbms() {
			return liquibaseDbms;
		}
		
		public static CustomDialectDatabase findByProductName(String productName) {
			for (CustomDialectDatabase db : values()) {
				if (db.productName.equalsIgnoreCase(productName))
					return db;
			}
			return null;
		}

	}
	
	@Override
	protected Database createDatabase(Connection c, ResourceAccessor resourceAccessor) throws DatabaseException {
		Database database = getDatabase(c);
		if (CustomDialectDatabase.SQLITE.getProductName().equals(database.getDatabaseProductName()) ||
			CustomDialectDatabase.SQLITE_ANDROID.getProductName().equals(database.getDatabaseProductName())) {
			// schemas are not supported
			return database;
		} else {
			return super.createDatabase(c, resourceAccessor);
		}
	}

	@Override
	protected Liquibase createLiquibase(Connection c) throws LiquibaseException {
		Database database = createDatabase(c, null);
		String changeLog = getChangeLog().replaceAll(DBMS_PLACEHOLDER, getMigrationDialect(database));
		Liquibase liquibase = new Liquibase(changeLog, createResourceOpener(), database);
		if (isDropFirst()) {
			liquibase.dropAll();
		}
		return liquibase;
	}

	private String getMigrationDialect(Database database) {
		String dbProductName = database.getDatabaseProductName();
		CustomDialectDatabase customDialectDb = CustomDialectDatabase.findByProductName(dbProductName);
		return customDialectDb == null ? STANDARD_DIALECT : customDialectDb.getLiquibaseDbms();
	}

	private Database getDatabase(Connection c) throws DatabaseException {
		return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
	}

}