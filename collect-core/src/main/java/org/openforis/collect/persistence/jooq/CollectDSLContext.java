package org.openforis.collect.persistence.jooq;

import java.sql.Connection;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Sequence;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DialectAwareJooqConfiguration;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectDSLContext extends DefaultDSLContext {

	private static final long serialVersionUID = 1L;
	
	public CollectDSLContext(Connection connection) {
		this(new DialectAwareJooqConfiguration(connection));
	}
	
	public CollectDSLContext(Configuration conf) {
		super(conf);
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
		if (isSequenceSupported()) {
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
			default:
				throw new RuntimeException("DB dialeg not supported : " + configuration().dialect());
			}
		}
	}
	
	private boolean isSequenceSupported() {
		switch (configuration().dialect()) {
		case SQLITE:
			return false;
		default:
			return true;
		}
	}
	
	public boolean isSchemaLess() {
		return ! configuration().settings().isRenderSchema();
	}
	
	public boolean isForeignKeySupported() {
		return ! isSQLite();
	}
	
	public boolean isSQLite() {
		return getDialect() == SQLDialect.SQLITE;
	}
	
	public SQLDialect getDialect() {
		return configuration().dialect();
	}
}
