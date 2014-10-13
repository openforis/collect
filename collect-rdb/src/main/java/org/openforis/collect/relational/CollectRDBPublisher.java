package org.openforis.collect.relational;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.jooq.JooqDatabaseExporter;
import org.openforis.collect.relational.liquibase.LiquibaseRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Transactional
public class CollectRDBPublisher {
	
	protected static Log LOG = LogFactory.getLog(CollectRDBPublisher.class);

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;
	@Autowired(required=false)
	@Qualifier("rdbDataSource")
	private DataSource rdbDataSource;
	
	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName) throws CollectRdbException {
		export(surveyName, rootEntityName, step, targetSchemaName, RelationalSchemaConfig.createDefault());
	}
	
	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName, RelationalSchemaConfig config) throws CollectRdbException {
		Connection targetConn = getTargetConnection();
		export(surveyName, rootEntityName, step, targetSchemaName, targetConn, config);
	}

	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName, Connection targetConn, RelationalSchemaConfig config) throws CollectRdbException {
		CollectSurvey survey = surveyManager.get(surveyName);
		
		// Generate relational model
		RelationalSchemaGenerator schemaGenerator = new RelationalSchemaGenerator(config);
		RelationalSchema relationalSchema = schemaGenerator.generateSchema(survey, targetSchemaName);
		
		RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		relationalSchemaCreator.createRelationalSchema(relationalSchema, targetConn);
		
		// Insert data
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName, step);
		int total = summaries.size();
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Total records: " + total);
		}
		insertRecords(survey, summaries, step, relationalSchema, targetConn);
		if ( LOG.isInfoEnabled() ) {
			LOG.info("\nAll records exported");
		}
	}
	
	public void exportToSQL(Writer writer, int surveyId, boolean work, String targetSchemaName, boolean includeData) throws CollectRdbException, IOException {
		RelationalSchemaConfig config = RelationalSchemaConfig.createDefault();
		CollectSurvey survey;
		if ( work ) {
			survey = surveyManager.loadSurveyWork(surveyId);
		} else {
			survey = surveyManager.getById(surveyId);
		}
		RelationalSchemaGenerator schemaGenerator = new RelationalSchemaGenerator(config);
		RelationalSchema schema = schemaGenerator.generateSchema(survey, targetSchemaName);
		
		new SQLRelationalSchemaCreator().writeRelationalSchema(writer, schema);
	}
	
	@Transactional("rdbTransactionManager")
	private void insertRecords(CollectSurvey survey, List<CollectRecord> summaries, Step step, 
			RelationalSchema targetSchema, Connection targetConn) throws CollectRdbException {
		DatabaseExporter databaseExporter = new JooqDatabaseExporter(targetConn);
		databaseExporter.insertReferenceData(targetSchema);
		for (int i = 0; i < summaries.size(); i++) {
			CollectRecord summary = summaries.get(i);
//			if ( LOG.isInfoEnabled() ) {
//				LOG.info("Exporting record #" + (++i) + " id: " + summary.getId());
//			}
			CollectRecord record = recordManager.load(survey, summary.getId(), step);
			databaseExporter.insertData(targetSchema, record);
		}
		try {
			targetConn.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Error inserting records into relational database", e);
		}
	}
	
	private Connection getTargetConnection() {
		DataSource targetDataSource = rdbDataSource == null ? dataSource: rdbDataSource;
		Connection targetConn = DataSourceUtils.getConnection(targetDataSource);
		return targetConn;
	}
	/*
	public static void main(String[] args) throws CollectRdbException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml");
		CollectRDBPublisher publisher = ctx.getBean(CollectRDBPublisher.class);
		RelationalSchemaConfig config = RelationalSchemaConfig.createDefault();
//		config.setDefaultCode(null);
		publisher.export(
				"naforma1",
				"cluster",
				Step.ANALYSIS,
				"naforma1",
				config);
//		DriverManager.getConnection("jdbc:postgresql://localhost:5433/archenland1", "postgres","postgres")); 
	}
	*/
}
