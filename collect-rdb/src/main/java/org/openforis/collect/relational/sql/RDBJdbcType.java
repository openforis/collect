package org.openforis.collect.relational.sql;

import java.sql.Types;
import java.util.Date;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public enum RDBJdbcType {

	INTEGER(Types.INTEGER, "integer", Integer.class),
	FLOAT(Types.FLOAT, "float", Double.class),
	BIGINT(Types.BIGINT, "bigint", Long.class),
	BOOLEAN(Types.BOOLEAN, "boolean", Boolean.class),
	VARCHAR(Types.VARCHAR, "varchar", String.class), 
	DATE(Types.DATE, "date", Date.class),
	TIME(Types.TIME, "time", Date.class);
	
	private int code;
	private String name;
	private Class<?> javaType;

	public static RDBJdbcType fromType(Class<?> type) {
		if ( type == Integer.class ) {
			return RDBJdbcType.INTEGER;
		} else if ( type == Double.class ) {
			return RDBJdbcType.FLOAT;
		} else if ( type == Long.class ) {
			return RDBJdbcType.BIGINT;
		} else if ( type == Boolean.class ) {
			return RDBJdbcType.BOOLEAN;
		} else if ( type == String.class ) {
			return RDBJdbcType.VARCHAR;
		} else {
			throw new UnsupportedOperationException("Unknown field type "+type);				
		}
	}
	
	public static RDBJdbcType fromCompositeAttributeDefinition(AttributeDefinition defn) {
		if ( defn instanceof DateAttributeDefinition ) {
			return RDBJdbcType.DATE;
		} else if ( defn instanceof TimeAttributeDefinition ) {
			return RDBJdbcType.TIME;
		} else {
			throw new UnsupportedOperationException("Unsupported composite attribute definition: " + defn.getClass());
		}
	}
	
	private RDBJdbcType(int code, String name, Class<?> javaType) {
		this.code = code;
		this.name = name;
		this.javaType = javaType;
	}

	public int getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}
	
	public Class<?> getJavaType() {
		return javaType;
	}
	
}
