package org.openforis.collect.persistence.liquibase.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

public abstract class Migrations {
	
	private Map<String, Migration> migrationByDBProductName;
	
	protected Migrations(Map<String, Migration> migrationByDBProductName) {
		this.migrationByDBProductName = migrationByDBProductName;
	}
	
	public void execute(Connection c, String databaseProductName) throws SQLException {
		Migration migration = migrationByDBProductName.get(databaseProductName.toLowerCase(Locale.ENGLISH));
		if (migration != null) {
			migration.execute(c);
		}
	}
}
