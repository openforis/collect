package org.openforis.collect.relational.liquibase;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;
import org.openforis.collect.relational.CollectRelationalTest;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;

/**
 * 
 * @author G. Miceli
 *
 */
public class DatabaseSyncTest extends CollectRelationalTest {

	@Test
	public void testSync() throws Exception {
		String targetSchema = "archenland";
		Connection targetConn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/archenland1", "postgres","postgres");
		
		RelationalSchemaGenerator rsg = new RelationalSchemaGenerator();
		RelationalSchema schema = rsg.generateSchema(survey, targetSchema);
		RelationalSchemaCreator relationalSchemaCreator = new RelationalSchemaCreator();
		relationalSchemaCreator.createRelationalSchema(schema, targetConn);
		System.out.println("ok");
//		CollectRecord record = createTestRecord(survey, "123_456");
		
		// TODO proper integration test
	}
}
