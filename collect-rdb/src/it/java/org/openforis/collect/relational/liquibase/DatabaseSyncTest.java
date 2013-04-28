package org.openforis.collect.relational.liquibase;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Test;
import org.openforis.collect.jooq.JooqDatabaseExporter;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
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
		// Inputs
		String targetSchema = "archenland";
		Connection targetConn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/archenland1", "postgres","postgres");
		CollectRecord record = createTestRecord(survey, "123_456");
		
		// Generate relational model
		RelationalSchemaGenerator rsg = new RelationalSchemaGenerator();
		RelationalSchema schema = rsg.generateSchema(survey, targetSchema);
		
		// Create schema in database
		LiquibaseRelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		relationalSchemaCreator.createRelationalSchema(schema, targetConn);
		System.out.println("schemagen ok");
		
		// Insert data
		JooqDatabaseExporter exporter = new JooqDatabaseExporter(new DialectAwareJooqFactory(targetConn));
		exporter.insertData(schema, record);
		System.out.println("inserts ok");
	}
}
