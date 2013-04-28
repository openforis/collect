package org.openforis.collect.relational;

import java.sql.Connection;

import org.openforis.collect.relational.model.RelationalSchema;

/**
 * 
 * @author G. Miceli
 *
 */
public interface RelationalSchemaCreator {
	void createRelationalSchema(RelationalSchema schema, Connection conn) throws CollectRdbException;
}
