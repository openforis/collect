package org.openforis.collect.persistence.jooq;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Sequence;
import org.jooq.TableField;
import org.jooq.conf.Settings;
import org.jooq.impl.Factory;

/**
 * @author G. Miceli
 */
public class DialectAwareJooqFactory extends Factory {
	
	private static final long serialVersionUID = 1L;

	private enum Database {
		POSTGRES("PostgreSQL", SQLDialect.POSTGRES),
		DERBY("Apache Derby", SQLDialect.DERBY),
		SQLITE("SQLite", SQLDialect.SQLITE),
		SQLITE_FOR_ANDROID("SQLite for Android", SQLDialect.SQLITE);
		
		private String productName;
		private SQLDialect dialect;

		Database(String productName, SQLDialect dialect) {
			this.productName = productName;
			this.dialect = dialect;
		}
		
		public static Database getByProductName(String name) {
			for (Database db : Database.values()) {
				if ( name.equals(db.getProductName())) {
					return db;
				}
			}
			return null;
		}
		
		public String getProductName() {
			return productName;
		}
		
		public SQLDialect getDialect() {
			return dialect;
		}
	}
	
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
			Database db = Database.getByProductName(dbName);
			if ( db == null ) {
				throw new IllegalArgumentException("Unknown database "+dbName);
			}
			return db.getDialect();
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
	
	public void restartSequence(Sequence<?> sequence, Number restartValue) {
		String name = sequence.getName();
		String qualifiedName;
		if ( sequence.getSchema() != null && getSettings().isRenderSchema() ) {
			Schema schema = sequence.getSchema();
			String schemaName = schema.getName();
			qualifiedName = schemaName + "." + name;
		} else {
			qualifiedName = name;
		}
		switch (getDialect()) {
		case POSTGRES:
			execute("ALTER SEQUENCE " +  qualifiedName + " RESTART WITH " + restartValue);
			break;
		case SQLITE:
			//sequences not handled
			break;
		}
	}

}
