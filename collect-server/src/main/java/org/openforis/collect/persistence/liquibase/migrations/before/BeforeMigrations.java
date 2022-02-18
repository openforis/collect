package org.openforis.collect.persistence.liquibase.migrations.before;

import java.util.Map;

import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.liquibase.migrations.Migrations;
import org.openforis.collect.persistence.liquibase.migrations.before.sqlite.BeforeMigrationsSQLite;

public class BeforeMigrations extends Migrations {

	private static final Map<String, Migration> BEFORE_MIGRATIONS = Map.of("sqlite", new BeforeMigrationsSQLite());

	public BeforeMigrations() {
		super(BEFORE_MIGRATIONS);
	}

}
