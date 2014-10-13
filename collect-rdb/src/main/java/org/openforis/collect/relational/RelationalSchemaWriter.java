package org.openforis.collect.relational;

import java.io.IOException;
import java.io.Writer;

import org.openforis.collect.relational.model.RelationalSchema;

/**
 * 
 * @author S. Ricci
 * 
 */
public interface RelationalSchemaWriter {
	
	void writeRelationalSchema(Writer writer, RelationalSchema schema) throws IOException;
	
	void writeRelationalSchema(Writer writer, RelationalSchema schema, String databaseProductName) throws IOException;
}
