package org.openforis.collect.persistence.jooq;

import static org.jooq.impl.DSL.name;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;

import org.apache.poi.ss.formula.functions.T;
import org.jooq.CollectCreateIndexStep;
import org.jooq.Configuration;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Sequence;
import org.jooq.TableField;
import org.jooq.impl.CollectCreateIndexImpl;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.DialectAwareJooqConfiguration;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectDSLContext extends DefaultDSLContext {

	private static final long serialVersionUID = 1L;
	
	public CollectDSLContext(Configuration config) {
		super(config);
	}
	
	public CollectDSLContext(Connection connection) {
		super(new DialectAwareJooqConfiguration(connection));
	}

	@Override
    public CollectCreateIndexStep createIndex(String index) {
        return createIndex(name(index));
    }

    @Override
    public CollectCreateIndexStep createIndex(Name index) {
        return new CollectCreateIndexImpl(configuration(), index);
    }

	public <I extends Number> I nextId(TableField<?, I> idField, Sequence<? extends Number> idSequence) {
		if (isSQLite()){
			I id = (I) select(DSL.max(idField))
					.from(idField.getTable())
					.fetchOne(0);
			Long result = id == null ? 1 : id.longValue() + 1;
			return idField.getType() == Integer.class ? (I) new Integer(result.intValue()) : (I) result;
		} else {
			return (I) nextval(idSequence);	
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
			execute("ALTER SEQUENCE " +  qualifiedName + " RESTART WITH " + restartValue);
		}
	}
	
	public DataType<?> getDataType(Class<?> type) {
		Class<?> jooqType;
		if (type == Date.class) {
			jooqType = java.sql.Date.class;
		} else if (type == Double.class && dialect() == SQLDialect.SQLITE) {
			jooqType = Float.class; //it will be translated into an SQL REAL data type
		} else {
			jooqType = type;
		}
		return DefaultDataType.getDataType(dialect(), jooqType);
	}
	
	private boolean isSequenceSupported() {
		return configuration().dialect() != SQLDialect.SQLITE;
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
