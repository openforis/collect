package org.openforis.collect.persistence.liquibase.migrations.before.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.liquibase.migrations.SQLiteUtils;
import org.openforis.commons.versioning.Version;

public class BeforeMigrationsSQLite implements Migration {

	private Map<String, Migration> migrationsByVersion;

	public BeforeMigrationsSQLite() {
		migrationsByVersion = new LinkedHashMap<>(); // keep migrations ordered
		migrationsByVersion.put("4.0.23", new BeforeMigration001FixSurveyUsergroupFK());
	}

	@Override
	public void execute(Connection c) throws SQLException {
		Version dbVersion = SQLiteUtils.readVersionFromDb(c);
		if (dbVersion != null) {
			Set<Entry<String, Migration>> entrySet = migrationsByVersion.entrySet();
			Iterator<Entry<String, Migration>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<String, Migration> entry = iterator.next();
				Version migrationVersion = new Version(entry.getKey());
				if (dbVersion.compareTo(migrationVersion) < 0) {
					entry.getValue().execute(c);
				}
			}

		}
	}
}
