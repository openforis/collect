package org.openforis.collect.relational;

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
import org.openforis.collect.relational.jooq.JooqRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.concurrency.ProgressListener;
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
		
		RelationalSchemaCreator relationalSchemaCreator = createRelationalSchemaCreator();
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

	private void insertRecords(CollectSurvey survey, List<CollectRecord> summaries, Step step, 
			RelationalSchema targetSchema, Connection targetConn) throws CollectRdbException {
		try {
			targetConn.setAutoCommit(false);
		} catch (SQLException e1) {
		}
		DatabaseExporter databaseExporter = createDatabaseExporter(targetConn);
		databaseExporter.insertReferenceData(targetSchema, ProgressListener.NULL_PROGRESS_LISTENER);
		for (int i = 0; i < summaries.size(); i++) {
			CollectRecord summary = summaries.get(i);
//			if ( LOG.isInfoEnabled() ) {
//				LOG.info("Exporting record #" + (++i) + " id: " + summary.getId());
//			}
			CollectRecord record = recordManager.load(survey, summary.getId(), step, false);
			databaseExporter.insertRecordData(targetSchema, record, ProgressListener.NULL_PROGRESS_LISTENER);
		}
		try {
			targetConn.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Error inserting records into relational database", e);
		}
	}

	private Connection getTargetConnection() {
		// dO NOT REMOVE THIS, IT IS NECESSARY FOR sAIKU IN cOLLECT eARTH!
		DataSource targetDataSource = rdbDataSource == null ? dataSource: rdbDataSource;
		Connection targetConn = DataSourceUtils.getConnection(targetDataSource);
		return targetConn;
	}
	
	private JooqRelationalSchemaCreator createRelationalSchemaCreator() {
		return new JooqRelationalSchemaCreator();
	}
	
	private DatabaseExporter createDatabaseExporter(Connection targetConn) {
		return new JooqDatabaseExporter(targetConn);
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
