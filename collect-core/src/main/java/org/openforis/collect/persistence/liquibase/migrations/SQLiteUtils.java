package org.openforis.collect.persistence.liquibase.migrations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.openforis.collect.persistence.utils.DBUtils;
import org.openforis.commons.versioning.Version;

public abstract class SQLiteUtils {

	public static Version readVersionFromDb(Connection c) {
		String query = "SELECT \"version\" FROM ofc_application_info";
		Statement stmt = null;
		try {
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				String versionString = rs.getString(1);
				return new Version(versionString);
			}
		} catch (Exception e) {
			// ignore it, ofc_application_info table not existing already
		} finally {
			DBUtils.closeQuietly(stmt);
		}
		return null;
	}

}
