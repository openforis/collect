package org.openforis.collect.persistence.liquibase.migrations.after;

import java.util.Map;

import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.liquibase.migrations.Migrations;
import org.openforis.collect.persistence.liquibase.migrations.after.sqlite.AfterMigrationsSQLite;

public class AfterMigrations extends Migrations {

	private static final Map<String, Migration> AFTER_MIGRATIONS = Map.of("sqlite", new AfterMigrationsSQLite());

	public AfterMigrations() {
		super(AFTER_MIGRATIONS);
	}

}
