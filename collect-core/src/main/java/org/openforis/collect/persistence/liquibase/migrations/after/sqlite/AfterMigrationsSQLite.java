package org.openforis.collect.persistence.liquibase.migrations.after.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.Collect;
import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.liquibase.migrations.SQLiteUtils;
import org.openforis.collect.persistence.utils.DBUtils;
import org.openforis.commons.versioning.Version;

public class AfterMigrationsSQLite implements Migration {
	
	private static final Logger LOG = LogManager.getLogger(AfterMigrationsSQLite.class);

	@Override
	public void execute(Connection c) throws SQLException {
		LOG.info("Running DB migrations after executing Liquibase (SQLite DB)");
		Version dbVersion = SQLiteUtils.readVersionFromDb(c);
		LOG.info(String.format("Application version found: %s", dbVersion == null ? "-" : dbVersion.toString()));

		if (dbVersion == null || dbVersion.compareTo(Collect.VERSION) < 0) {
			LOG.info("Updating application version in DB...");

			PreparedStatement stmt = null;
			try {
				stmt = c.prepareStatement("UPDATE ofc_application_info SET \"version\" = ?");
				stmt.setString(1, Collect.VERSION.toString());
				stmt.executeUpdate();
				
				LOG.info("Application version updated successfully!");
			} finally {
				DBUtils.closeQuietly(stmt);
			}
		}
	}
}
