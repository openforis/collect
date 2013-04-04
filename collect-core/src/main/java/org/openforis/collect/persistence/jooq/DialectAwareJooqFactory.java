package org.openforis.collect.persistence.jooq;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.jooq.SQLDialect;
import org.jooq.Sequence;
import org.jooq.TableField;
import org.jooq.conf.Settings;
import org.jooq.impl.Factory;

/**
 * @author G. Miceli
 */
public class DialectAwareJooqFactory extends Factory {
	
	private static final long serialVersionUID = 1L;
	private static final String POSTGRESQL_DBNAME = "PostgreSQL";
	private static final Object SQLITE_DBNAME = "SQLite";

	public DialectAwareJooqFactory(Connection connection) {
		this(connection, getDialect(connection));
	}

	public DialectAwareJooqFactory(Connection connection, SQLDialect dialect) {
		super(connection, dialect, createSettings(dialect));
	}

	private static SQLDialect getDialect(Connection conn) {
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			String dbName = metaData.getDatabaseProductName();
			if ( dbName.equals(POSTGRESQL_DBNAME) ) {
				return SQLDialect.POSTGRES;
			} else if ( dbName.equals(SQLITE_DBNAME) ) {
				return SQLDialect.SQLITE;
			} else {
				throw new IllegalArgumentException("Unsupported database: "+dbName);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error getting database name", e);
		}
	}

	private static Settings createSettings(SQLDialect dialect) {
		Settings settings = new Settings();
		if ( dialect == SQLDialect.SQLITE ) {
			settings.withRenderSchema(false);
		}  
		return settings;
	}
	
	public int nextId(TableField<?, Integer> idField, Sequence<? extends Number> idSequence) {
		if (getDialect() == SQLDialect.SQLITE){
			Integer id = (Integer) select(max(idField)).from(idField.getTable()).fetchOne(0);
			if ( id == null ) {
				return 1;
			} else {
				return id + 1;
			}
		} else {
			return nextval(idSequence).intValue();	
		}	
	}

}
