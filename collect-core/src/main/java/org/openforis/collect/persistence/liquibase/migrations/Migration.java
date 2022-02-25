package org.openforis.collect.persistence.liquibase.migrations;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration {
	
	public void execute(Connection c, String schemaName) throws SQLException;

}
