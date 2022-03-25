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
public class DatabaseAwareSpringLiquibase extends SpringLiquibase {

	private static final String STANDARD_DIALECT = "standard";
	private static final String DBMS_PLACEHOLDER = "DBMS_ID";

	@Override
	protected Database createDatabase(Connection c, ResourceAccessor resourceAccessor) throws DatabaseException {
		Database database = getDatabase(c);
		if (LiquibaseSupportedDBMS.SQLITE.getProductName().equals(database.getDatabaseProductName())
				|| LiquibaseSupportedDBMS.SQLITE_ANDROID.getProductName().equals(database.getDatabaseProductName())) {
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
			String schemaName = liquibase.getDatabase().getDefaultSchemaName();

			Connection connection = ((JdbcConnection) liquibase.getDatabase().getConnection()).getWrappedConnection();

			// before running Liquibase
			
			new BeforeMigrations().execute(connection, dbProductName, schemaName);

			super.performUpdate(liquibase);

			// after running Liquibase
			new AfterMigrations().execute(connection, dbProductName, schemaName);
		} catch (Exception e) {
			throw new LiquibaseException(e);
		}
	}

	private String getMigrationDialect(Database database) {
		String dbProductName = database.getDatabaseProductName();
		LiquibaseSupportedDBMS customDialectDb = LiquibaseSupportedDBMS.findByProductName(dbProductName);
		return customDialectDb == null ? STANDARD_DIALECT : customDialectDb.getLiquibaseDbms();
	}

	private Database getDatabase(Connection c) throws DatabaseException {
		return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
	}

}