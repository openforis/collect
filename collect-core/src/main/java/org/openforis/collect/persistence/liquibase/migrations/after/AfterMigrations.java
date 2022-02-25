package org.openforis.collect.persistence.liquibase.migrations.after;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.persistence.liquibase.LiquibaseSupportedDBMS;
import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.liquibase.migrations.Migrations;
import org.openforis.collect.persistence.liquibase.migrations.after.standard.AfterMigrationsStandard;

public class AfterMigrations extends Migrations {

	private static final Map<String, Migration> AFTER_MIGRATIONS;
	static {
		AfterMigrationsStandard standardMigrations = new AfterMigrationsStandard();
		AFTER_MIGRATIONS = new HashMap<String, Migration>();
		List<LiquibaseSupportedDBMS> list = Arrays.asList(LiquibaseSupportedDBMS.H2, LiquibaseSupportedDBMS.POSTGRESQL,
				LiquibaseSupportedDBMS.SQLITE);
		for (LiquibaseSupportedDBMS standardMigrationsDbms : list) {
			AFTER_MIGRATIONS.put(standardMigrationsDbms.getProductName().toLowerCase(Locale.ENGLISH),
					standardMigrations);
		}
	}

	public AfterMigrations() {
		super(AFTER_MIGRATIONS);
	}

}
