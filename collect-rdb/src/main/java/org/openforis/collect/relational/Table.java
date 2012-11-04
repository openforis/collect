package org.openforis.collect.relational;

import java.util.List;

/**
 * 
 * @author G. Miceli
 *
 */
public interface Table<T> {

	String getPrefix();
	
	String getName();

	String getFullName();
	
	List<Column<?>> getColumns();

	PrimaryKeyConstraint getPrimaryKeyConstraint();
//	List<UniquenessConstraint> getUniquenessConstraints();

	List<ReferentialConstraint> getReferentialContraints();

//	Dataset extractData(T source);
	
	Row extractRow(T source);
}