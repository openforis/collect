/**
 * 
 */
package org.openforis.collect.persistence.liquibase;

import java.sql.Connection;
import java.util.Map;

import org.openforis.collect.persistence.liquibase.migrations.after.AfterMigrations;
import org.openforis.collect.persistence.liquibase.migrations.before.BeforeMigrations;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.integration.spring.SpringResourceAccessor;
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
		if (CustomDialectDatabase.SQLITE.getProductName().equals(database.getDatabaseProductName())
				|| CustomDialectDatabase.SQLITE_ANDROID.getProductName().equals(database.getDatabaseProductName())) {
			// schemas are not supported
			return database;
		} else {
			return super.createDatabase(c, resourceAccessor);
		}
	}

	@Override
	protected Liquibase createLiquibase(Connection c) throws LiquibaseException {
		SpringResourceAccessor resourceAccessor = createResourceOpener();
		Database database = createDatabase(c, resourceAccessor);

		String changeLog = getChangeLog().replace(DBMS_PLACEHOLDER, getMigrationDialect(database));
		Liquibase liquibase = new Liquibase(changeLog, resourceAccessor, database);
		if (parameters != null) {
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
			}
		}
		if (isDropFirst()) {
			liquibase.dropAll();
		}
		return liquibase;
	}

	@Override
	protected void performUpdate(Liquibase liquibase) throws LiquibaseException {
		try {
			String dbProductName = liquibase.getDatabase().getDatabaseProductName();

			// before running Liquibase
			new BeforeMigrations().execute(getDataSource().getConnection(), dbProductName);

			super.performUpdate(liquibase);

			// after running Liquibase
			new AfterMigrations().execute(getDataSource().getConnection(), dbProductName);
		} catch (Exception e) {
			throw new LiquibaseException(e);
		}
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