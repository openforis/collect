package org.openforis.collect.persistence.liquibase.migrations.before.sqlite;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.persistence.liquibase.migrations.Migration;
import org.openforis.collect.persistence.utils.DBUtils;

public class BeforeMigration001FixSurveyUsergroupFK implements Migration {

	private static final Logger LOG = LogManager.getLogger(BeforeMigration001FixSurveyUsergroupFK.class);
	private static final String SQL_FILE_NAME = "before_migration_001_fix_survey_usergroup_fk.sql";

	public void execute(Connection c) throws SQLException {
		LOG.info("Running migration 001FixSurveyUsergroupFK...");
		InputStream is = null;
		Statement stmt = null;
		try {
			is = BeforeMigration001FixSurveyUsergroupFK.class.getResourceAsStream(SQL_FILE_NAME);
			String sql = IOUtils.toString(is, StandardCharsets.UTF_8);
			stmt = c.createStatement();
			stmt.executeUpdate(sql);
			LOG.info("Migration run successfully!");
		} catch (Exception e) {
			throw new SQLException(e);
		} finally {
			IOUtils.closeQuietly(is);
			DBUtils.closeQuietly(stmt);
		}
	}
	
}
