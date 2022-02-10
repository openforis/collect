package org.openforis.collect.relational.util;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class DBUtils {

	public static void closeQuietly(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				// ignore it
			}
		}
	}

}
