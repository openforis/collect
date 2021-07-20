package org.openforis.collect.persistence.liquibase.migrations.after.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.openforis.collect.Collect;
import org.openforis.collect.persistence.liquibase.migrations.Migration;

public class AfterMigrationsSQLite implements Migration {

	@Override
	public void execute(Connection c) throws SQLException {
		// update application version
	    try (PreparedStatement stmt = c.prepareStatement("UPDATE ofc_application_info SET \"version\" = ?")) {
    		stmt.setString(1, Collect.VERSION.toString());
    		stmt.executeUpdate();
	    }
	}
}
