package org.openforis.collect.relational.model;

import java.util.List;

/**
 * 
 * @author G. Miceli
 *
 */
public interface Table<T> {

	String getPrefix();
	
	String getBaseName();

	String getName();
	
	List<Column<?>> getColumns();
	
	Column<?> getColumn(String name);

	PrimaryKeyConstraint getPrimaryKeyConstraint();
//	List<UniquenessConstraint> getUniquenessConstraints();

	List<ReferentialConstraint> getReferentialContraints();

}