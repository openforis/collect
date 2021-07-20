package org.openforis.collect.persistence.liquibase.migrations.after;

import java.util.HashMap;

import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.liquibase.migrations.Migrations;
import org.openforis.collect.persistence.liquibase.migrations.after.sqlite.AfterMigrationsSQLite;

public class AfterMigrations extends Migrations {

	public AfterMigrations() {
		super(new HashMap<String, Migration>() {
			private static final long serialVersionUID = 1L;
			{
				put("sqlite", new AfterMigrationsSQLite());
			}
		});
	}

}
