package org.openforis.collect.persistence.liquibase.migrations.before;

import java.util.HashMap;

import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.liquibase.migrations.Migrations;
import org.openforis.collect.persistence.liquibase.migrations.before.sqlite.BeforeMigrationsSQLite;

public class BeforeMigrations extends Migrations {

	public BeforeMigrations() {
		super(new HashMap<String, Migration>() {
			private static final long serialVersionUID = 1L;
			{
				put("sqlite", new BeforeMigrationsSQLite());
			}
		});
	}

}
