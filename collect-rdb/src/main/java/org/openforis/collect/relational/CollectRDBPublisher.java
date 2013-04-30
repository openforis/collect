package org.openforis.collect.relational;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.openforis.collect.jooq.JooqDatabaseExporter;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.relational.liquibase.LiquibaseRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectRDBPublisher {
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	
	public void export(String surveyName, String rootEntityName, Step step, Connection targetConnection, 
			String targetSchemaName) throws CollectRdbException {
		CollectSurvey survey = surveyManager.get(surveyName);
		
		// Generate relational model
		RelationalSchemaGenerator rsg = new RelationalSchemaGenerator();
		RelationalSchema targetSchema = rsg.generateSchema(survey, targetSchemaName);
		
		// Create schema in database
		RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		relationalSchemaCreator.createRelationalSchema(targetSchema, targetConnection);
		
		// Insert data
		DatabaseExporter exporter = new JooqDatabaseExporter(new DialectAwareJooqFactory(targetConnection));
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName);
		int total = summaries.size();
		System.out.println("Total records: " + total);
		int count = 0;
		for (CollectRecord summary : summaries) {
			System.out.println("Exporting record " + ++count + " among " + total + "... ");
			CollectRecord record = recordManager.load(survey, summary.getId(), step.getStepNumber());
			exporter.insertData(targetSchema, record);
			System.out.print("done!");
		}
		System.out.println("All record exported");
	}
	
	public static void main(String[] args) throws Throwable {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml");
		CollectRDBPublisher exporter = ctx.getBean(CollectRDBPublisher.class);
		exporter.export(
				"naforma1",
				"cluster",
				Step.ANALYSIS,
				DriverManager.getConnection("jdbc:postgresql://localhost:5432/archenland1", "postgres","postgres"), 
				"archenland");
	}

}
