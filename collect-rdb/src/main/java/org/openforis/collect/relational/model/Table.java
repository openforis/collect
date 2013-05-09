package org.openforis.collect.relational.model;

import java.util.List;

import org.openforis.collect.relational.DatabaseExporterConfig;

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

	PrimaryKeyConstraint getPrimaryKeyConstraint();
//	List<UniquenessConstraint> getUniquenessConstraints();

	List<ReferentialConstraint> getReferentialContraints();

	Dataset extractData(T source);
	
	Dataset extractData(DatabaseExporterConfig config, T source);
	
	Row extractRow(T source);

	Row extractRow(DatabaseExporterConfig config, T source);
	
}