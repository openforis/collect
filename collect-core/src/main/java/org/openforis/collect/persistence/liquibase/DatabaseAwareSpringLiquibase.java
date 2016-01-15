/**
 * 
 */
package org.openforis.collect.persistence.liquibase;

import java.sql.Connection;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.integration.spring.SpringLiquibase;

/**
 * @author S. Ricci
 *
 */
public class DatabaseAwareSpringLiquibase extends SpringLiquibase {

	private static final String SQLITE_DBNAME = "SQLite";

	@Override
	protected Database createDatabase(Connection c) throws DatabaseException {
		String dbProductName = getDatabaseProductName(c);
		if ( SQLITE_DBNAME.equals(dbProductName) ) {
			//schemas are not supported
			DatabaseFactory dbFactory = DatabaseFactory.getInstance();
			JdbcConnection jdbcConnection = new JdbcConnection(c);
			return dbFactory.findCorrectDatabaseImplementation(jdbcConnection);
		} else {
			return super.createDatabase(c);
		}
	}

	private String getDatabaseProductName(Connection c) throws DatabaseException {
		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
		return database.getDatabaseProductName();
	}
	
}