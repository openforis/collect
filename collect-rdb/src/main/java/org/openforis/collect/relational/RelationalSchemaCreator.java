package org.openforis.collect.relational;

/**
 * 
 * @author G. Miceli
 *
 */
public interface RelationalSchemaCreator {
	
	void createRelationalSchema() throws CollectRdbException;

	void addConstraints();

	void addIndexes();
	
}
