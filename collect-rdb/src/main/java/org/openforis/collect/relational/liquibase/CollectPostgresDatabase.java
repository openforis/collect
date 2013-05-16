package org.openforis.collect.relational.liquibase;

import liquibase.database.DatabaseConnection;
import liquibase.database.core.PostgresDatabase;

/**
 * 
 * @author G. Miceli
 *
 */
class CollectPostgresDatabase extends PostgresDatabase {

	public CollectPostgresDatabase(DatabaseConnection dbconn) {
		setConnection(dbconn);
	}

	@Override
	public String escapeDatabaseObject(String objectName) {
		return "\"" + objectName.toLowerCase() + "\"";
	}
}
