package org.openforis.collect.persistence.liquibase.migrations.before.sqlite;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.persistence.liquibase.migrations.Migration;

public class BeforeMigration001FixSurveyUsergroupFK implements Migration {

	private static final String SQL_FILE_NAME = "before_migration_001_fix_survey_usergroup_fk.sql";

	public void execute(Connection c) throws SQLException {
		try (InputStream is = BeforeMigration001FixSurveyUsergroupFK.class.getResourceAsStream(SQL_FILE_NAME)) {
			String sql = IOUtils.toString(is, StandardCharsets.UTF_8);
			try (Statement stmt = c.createStatement()) {
				stmt.executeUpdate(sql);
			}
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}
	
}
