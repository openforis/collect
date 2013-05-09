package org.openforis.collect.relational.model;

import org.openforis.collect.relational.DatabaseExporterConfig;

/**
 * 
 * @author G. Miceli
 *
 * @param <T>
 */
public interface Column<T> {

	String getName();

	/**
	 * JDBC type from java.sql.Types
	 * @return
	 */
	int getType();

	String getTypeName();
	
	Integer getLength();

	boolean isNullable();
	
	Object extractValue(T source);
	
	Object extractValue(DatabaseExporterConfig config, T source);

}