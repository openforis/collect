package org.openforis.collect.persistence.liquibase.migrations.before.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.commons.versioning.Version;

public class BeforeMigrationsSQLite implements Migration {

	private LinkedHashMap<String, Migration> migrationsByVersion = new LinkedHashMap<String, Migration>() {
		private static final long serialVersionUID = 1L;
		{
			put("4.0.23", new BeforeMigration_001_FixSurveyUsergroupFK());
		}
	};

	private Version readVersionFromDb(Connection c) {
		String query = "SELECT \"version\" FROM ofc_application_info";
		try (Statement stmt = c.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				String versionString = rs.getString(1);
				return new Version(versionString);
			}
		} catch (Exception e) {
			// ignore it
		}
		return null;
	}

	@Override
	public void execute(Connection c) throws SQLException {
		Version version = readVersionFromDb(c);
		if (version != null) {
			Set<Entry<String, Migration>> entrySet = migrationsByVersion.entrySet();
			Iterator<Entry<String, Migration>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<String, Migration> entry = iterator.next();
				if (version.compareTo(new Version(entry.getKey())) < 0) {
					entry.getValue().execute(c);
				}
			}

		}
	}
}
