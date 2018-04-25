package org.openforis.collect.relational;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.relational.jooq.JooqDatabaseExporter;
import org.openforis.collect.relational.jooq.JooqRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.commons.collection.Visitor;
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
			String targetSchemaName, RelationalSchemaConfig config, ProgressListener progressListener) throws CollectRdbException {
		Connection targetConn = getTargetConnection();
		export(surveyName, rootEntityName, step, targetSchemaName, targetConn, config, progressListener);
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
			
			RelationalSchemaCreator relationalSchemaCreator = createRelationalSchemaCreator(relationalSchema, targetConn);
			relationalSchemaCreator.createRelationalSchema();

			relationalSchemaCreator.addConstraints();
			relationalSchemaCreator.addIndexes();
			
			insertData(survey, rootEntityName, step, targetConn, relationalSchema, progressListener);
			
			targetConn.commit();
			
			if ( LOG.isInfoEnabled() ) {
				LOG.info("RDB generation completed");
			}
		} catch (Exception e) {
			try {
				targetConn.rollback();
			} catch (SQLException e1) {
			}
			throw new RuntimeException(e);
		}
	}

	private void insertData(final CollectSurvey survey, String rootEntityName, final Step step, Connection targetConn,
			RelationalSchema relationalSchema, ProgressListener progressListener) throws IOException {
		// Insert data
		RecordFilter recordFilter = new RecordFilter(survey);
		recordFilter.setRootEntityId(survey.getSchema().getRootEntityDefinition(rootEntityName).getId());
		recordFilter.setStepGreaterOrEqual(step);
		
		final int total = recordManager.countRecords(recordFilter);
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Total records: " + total);
		}
		final DatabaseExporter databaseExporter = createDatabaseExporter(relationalSchema, targetConn);
		
		ProcessProgressListener totalProgressListener = new ProcessProgressListener(2);
		
		databaseExporter.insertReferenceData(new ProcessStepProgressListener(totalProgressListener, progressListener));
		
		final ProcessStepProgressListener insertRecordsProgressListener = new ProcessStepProgressListener(totalProgressListener, progressListener);
		
		final AtomicInteger count = new AtomicInteger();
		recordManager.visitSummaries(recordFilter, null, new Visitor<CollectRecordSummary>() {
			public void visit(CollectRecordSummary summary) {
				try {
					CollectRecord record = recordManager.load(survey, summary.getId(), step, false);
					databaseExporter.insertRecordData(record, ProgressListener.NULL_PROGRESS_LISTENER);
				} catch (CollectRdbException e) {
					LOG.error( e.getMessage(), e);
				}
				insertRecordsProgressListener.progressMade(new Progress(count.addAndGet(1), total));			
			}
		});
		
		databaseExporter.close();
		
		if ( LOG.isInfoEnabled() ) {
			LOG.info("All records exported");
			LOG.info("Adding constraints and indexes...");
		}
	}

	private Connection getTargetConnection() {
		// dO NOT REMOVE THIS, IT IS NECESSARY FOR sAIKU IN cOLLECT eARTH!
		DataSource targetDataSource = rdbDataSource == null ? dataSource: rdbDataSource;
		Connection targetConn = DataSourceUtils.getConnection(targetDataSource);
		return targetConn;
	}
	
	private JooqRelationalSchemaCreator createRelationalSchemaCreator(RelationalSchema relationalSchema, Connection conn) {
		return new JooqRelationalSchemaCreator(relationalSchema, conn);
	}
	
	private DatabaseExporter createDatabaseExporter(RelationalSchema schema, Connection targetConn) {
		return new JooqDatabaseExporter(schema, targetConn);
	}
	
}
