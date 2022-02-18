package org.openforis.collect.persistence.liquibase.migrations.before.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.openforis.collect.persistence.liquibase.migrations.Migration;

public class BeforeMigration001FixSurveyUsergroupFK implements Migration {

	public void execute(Connection c) throws SQLException {
		String sql = "PRAGMA foreign_keys=off;\n"
				+ "\n"
				+ "BEGIN TRANSACTION;\n"
				+ "\n"
				+ "CREATE TABLE _ofc_survey_new (\n"
				+ "  \"id\"	INTEGER NOT NULL,\n"
				+ "  \"name\"	TEXT NOT NULL,\n"
				+ "  \"uri\"	TEXT NOT NULL,\n"
				+ "  \"idml\"	TEXT NOT NULL,\n"
				+ "  \"target\"	varchar(5) NOT NULL DEFAULT 'CD',\n"
				+ "  \"date_created\"	timestamp,\n"
				+ "  \"date_modified\"	timestamp,\n"
				+ "  \"collect_version\"	varchar(55) NOT NULL DEFAULT '3.4.0',\n"
				+ "  \"temporary\"	bool NOT NULL DEFAULT 0,\n"
				+ "  \"published_id\"	INTEGER,\n"
				+ "  \"usergroup_id\"	INTEGER,\n"
				+ "  \"availability\"	char(1),\n"
				+ "  \"title\"	varchar(255),\n"
				+ "  \"langs\"	varchar(20),\n"
				+ "CONSTRAINT \"ofc_survey_name_key\" UNIQUE(\"name\",\"temporary\"),\n"
				+ "CONSTRAINT \"ofc_survey_uri_key\" UNIQUE(\"uri\",\"temporary\"),\n"
				+ "CONSTRAINT \"ofc_survey_pkey\" PRIMARY KEY(\"id\"),\n"
				+ "FOREIGN KEY(\"usergroup_id\") REFERENCES \"ofc_usergroup\"(\"id\")\n"
				+ ");\n"
				+ "\n"
				+ "INSERT INTO _ofc_survey_new (\n"
				+ "  \"id\", \"name\", \"uri\", \"idml\", \"target\", \"date_created\", \"date_modified\", \n"
				+ "  \"collect_version\", \"temporary\", \"published_id\", \"usergroup_id\", \"availability\",\n"
				+ "  \"title\", \"langs\")\n"
				+ "SELECT \n"
				+ "  \"id\", \"name\", \"uri\", \"idml\", \"target\", \"date_created\", \"date_modified\", \n"
				+ "  \"collect_version\", \"temporary\", \"published_id\", \"usergroup_id\", \"availability\",\n"
				+ "  \"title\", \"langs\" \n"
				+ "FROM ofc_survey;\n"
				+ "\n"
				+ "DROP TABLE ofc_survey;\n"
				+ "\n"
				+ "ALTER TABLE _ofc_survey_new RENAME TO ofc_survey;\n"
				+ "\n"
				+ "COMMIT;\n"
				+ "\n"
				+ "PRAGMA foreign_keys=on;";
		try (Statement stmt = c.createStatement()) {
			stmt.executeUpdate(sql);
		}
	}
	
}
