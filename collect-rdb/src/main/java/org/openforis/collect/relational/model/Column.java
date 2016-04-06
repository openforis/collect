package org.openforis.collect.relational.model;

import org.openforis.collect.relational.sql.RDBJdbcType;

/**
 * 
 * @author G. Miceli
 *
 * @param <T>
 */
public interface Column<T> {

	String getName();

	RDBJdbcType getType();

	Integer getLength();

	boolean isNullable();
	
}