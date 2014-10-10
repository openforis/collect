package org.openforis.collect.persistence.jooq;

import java.sql.Connection;

import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Sequence;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DialectAwareJooqConfiguration;

public class CollectDSLContext extends DefaultDSLContext {

	private static final long serialVersionUID = 1L;
	
	public CollectDSLContext(Connection connection) {
		super(new DialectAwareJooqConfiguration(connection));
	}
	
	public int nextId(TableField<?, Integer> idField, Sequence<? extends Number> idSequence) {
		if (isSQLite()){
			Integer id = (Integer) select(DSL.max(idField))
					.from(idField.getTable())
					.fetchOne(0);
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
		if ( sequence.getSchema() != null && configuration().settings().isRenderSchema() ) {
			Schema schema = sequence.getSchema();
			String schemaName = schema.getName();
			qualifiedName = schemaName + "." + name;
		} else {
			qualifiedName = name;
		}
		switch (configuration().dialect()) {
		case POSTGRES:
			execute("ALTER SEQUENCE " +  qualifiedName + " RESTART WITH " + restartValue);
			break;
		case SQLITE:
			//sequences not handled
			break;
		default:
			break;
		}
	}

	public boolean isSQLite() {
		return getDialect() == SQLDialect.SQLITE;
	}
	
	public SQLDialect getDialect() {
		return configuration().dialect();
	}
}
