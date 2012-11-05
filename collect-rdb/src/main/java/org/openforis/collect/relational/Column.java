package org.openforis.collect.relational;

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

	Integer getLength();

	boolean isNullable();
	
	Object extractValue(T source);

}