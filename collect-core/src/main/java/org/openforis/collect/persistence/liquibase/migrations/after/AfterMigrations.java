package org.openforis.collect.persistence.liquibase.migrations.after;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.liquibase.migrations.Migrations;
import org.openforis.collect.persistence.liquibase.migrations.after.sqlite.AfterMigrationsSQLite;

public class AfterMigrations extends Migrations {

	private static final Map<String, Migration> AFTER_MIGRATIONS;
	static {
		AFTER_MIGRATIONS = new HashMap<String, Migration>();
		AFTER_MIGRATIONS.put("sqlite", new AfterMigrationsSQLite());
	}

	public AfterMigrations() {
		super(AFTER_MIGRATIONS);
	}

}
