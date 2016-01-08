package org.openforis.collect.relational;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.relational.jooq.JooqDatabaseExporter;
import org.openforis.collect.relational.jooq.JooqRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.concurrency.ProcessProgressListener;
import org.openforis.concurrency.ProcessStepProgressListener;
import org.openforis.concurrency.Progress;
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
		export(surveyName, rootEntityName, step, targetSchemaName, targetConn, config, null);
	}
	
	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName, Connection targetConn, RelationalSchemaConfig config, ProgressListener progressListener) throws CollectRdbException {
		try {
			targetConn.setAutoCommit(false);
		} catch (SQLException e) {
		}
		try {
			CollectSurvey survey = surveyManager.get(surveyName);
			
			// Generate relational model
			RelationalSchemaGenerator schemaGenerator = new RelationalSchemaGenerator(config);
			RelationalSchema relationalSchema = schemaGenerator.generateSchema(survey, targetSchemaName);
			
			RelationalSchemaCreator relationalSchemaCreator = createRelationalSchemaCreator();
			relationalSchemaCreator.createRelationalSchema(relationalSchema, targetConn);
			
			// Insert data
			RecordFilter recordFilter = new RecordFilter(survey);
			recordFilter.setRootEntityId(survey.getSchema().getRootEntityDefinition(rootEntityName).getId());
			recordFilter.setStepGreaterOrEqual(step);
			
			int total = recordManager.countRecords(recordFilter);
			if ( LOG.isInfoEnabled() ) {
				LOG.info("Total records: " + total);
			}
			Iterator<CollectRecord> iterator = recordManager.iterateSummaries(recordFilter, null);
			
			DatabaseExporter databaseExporter = null;
			databaseExporter = createDatabaseExporter(relationalSchema, targetConn);
			
			ProcessProgressListener totalProgressListener = new ProcessProgressListener(2);
			
			databaseExporter.insertReferenceData(new ProcessStepProgressListener(totalProgressListener, progressListener));
			
			ProcessStepProgressListener insertRecordsProgressListener = new ProcessStepProgressListener(totalProgressListener, progressListener);
			
			int count = 0;
			while(iterator.hasNext()) {
				CollectRecord summary = iterator.next();
				CollectRecord record = recordManager.load(survey, summary.getId(), step, false);
				databaseExporter.insertRecordData(record, ProgressListener.NULL_PROGRESS_LISTENER);
				insertRecordsProgressListener.progressMade(new Progress(++count, total));
			}
			if ( LOG.isInfoEnabled() ) {
				LOG.info("\nAll records exported");
			}
			databaseExporter.close();
			
			targetConn.commit();
		} catch (Exception e) {
			try {
				targetConn.rollback();
			} catch (SQLException e1) {
			}
			throw new RuntimeException(e);
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
	
	private DatabaseExporter createDatabaseExporter(RelationalSchema schema, Connection targetConn) {
		return new JooqDatabaseExporter(schema, targetConn);
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
