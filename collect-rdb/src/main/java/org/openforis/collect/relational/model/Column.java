package org.openforis.collect.relational.model;


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
	
}