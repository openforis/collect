package org.openforis.collect.persistence.liquibase.migrations.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.openforis.collect.persistence.utils.DBUtils;
import org.openforis.commons.versioning.Version;

public abstract class CollectDbUtils {

	public static Version readVersionFromDb(Connection c, String schemaName) {
		String query = schemaName == null 
				? "SELECT \"version\" FROM ofc_application_info"
				: String.format("SELECT \"version\" FROM %s.ofc_application_info", schemaName);
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
