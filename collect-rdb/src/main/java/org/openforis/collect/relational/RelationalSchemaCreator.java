package org.openforis.collect.relational;

import java.sql.Connection;

import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.SchemaGenerationException;

/**
 * 
 * @author G. Miceli
 *
 */
public interface RelationalSchemaCreator {
	void createRelationalSchema(RelationalSchema schema, Connection targetConn) throws SchemaGenerationException;
}
