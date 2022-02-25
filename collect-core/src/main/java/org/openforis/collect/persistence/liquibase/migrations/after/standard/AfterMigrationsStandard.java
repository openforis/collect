package org.openforis.collect.persistence.liquibase.migrations.after.standard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.Collect;
import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.liquibase.migrations.utils.CollectDbUtils;
import org.openforis.collect.persistence.utils.DBUtils;
import org.openforis.commons.versioning.Version;

public class AfterMigrationsStandard implements Migration {
	
	private static final Logger LOG = LogManager.getLogger(AfterMigrationsStandard.class);

	@Override
	public void execute(Connection c, String schemaName) throws SQLException {
		LOG.info("Running DB migrations after executing Liquibase");

		Version dbVersion = CollectDbUtils.readVersionFromDb(c, schemaName);
		
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Application version found: %s", dbVersion == null ? "-" : dbVersion.toString()));
		}

		if (dbVersion == null || dbVersion.compareTo(Collect.VERSION) < 0) {
			LOG.info("Updating application version in DB...");

			PreparedStatement stmt = null;
			try {
				String query = schemaName == null
						? "UPDATE ofc_application_info SET \"version\" = ?"
						: String.format("UPDATE %s.ofc_application_info SET \"version\" = ?", schemaName);
				stmt = c.prepareStatement(query);
				stmt.setString(1, Collect.VERSION.toString());
				stmt.executeUpdate();
				
				LOG.info("Application version updated successfully!");
			} finally {
				DBUtils.closeQuietly(stmt);
			}
		} else {
			LOG.info("Migrations not necessary");
		}
	}
}
