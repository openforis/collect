package org.openforis.collect.relational.jooq;

import java.sql.Connection;
import java.util.List;

import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.Table;

/**
 * 
 * @author S. Ricci
 *
 */
public class JooqRelationalSchemaCreator implements RelationalSchemaCreator {

	
	@Override
	public void createRelationalSchema(RelationalSchema schema, Connection targetConn) throws CollectRdbException {
		CollectDSLContext dsl = new CollectDSLContext(targetConn);
		
		List<Table<?>> tables = schema.getTables();
		for (Table<?> table : tables) {
			
		}
	}

}
