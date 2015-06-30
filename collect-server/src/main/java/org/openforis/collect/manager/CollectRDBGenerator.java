package org.openforis.collect.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordDeletedEvent;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.relational.CollectLocalRDBStorageManager;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.jooq.JooqDatabaseExporter;
import org.openforis.collect.relational.liquibase.LiquibaseRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectRDBGenerator implements EventListener {

	private static final Log LOG = LogFactory.getLog(CollectRDBGenerator.class);
	private static final String SQLITE_DRIVER_CLASS_NAME = "org.sqlite.JDBC";

	private SurveyManager surveyManager;
	private RecordManager recordManager;
	private CollectLocalRDBStorageManager localRDBStorageManager;
	
	private Map<Integer, RelationalSchema> surveyIdToRelationalSchema;
	
	public CollectRDBGenerator(SurveyManager surveyManager, RecordManager recordManager, 
			CollectLocalRDBStorageManager localRDBStorageManager) {
		this.surveyManager = surveyManager;
		this.recordManager = recordManager;
		this.localRDBStorageManager = localRDBStorageManager;
		this.surveyIdToRelationalSchema = new HashMap<Integer, RelationalSchema>();
		
		init();
	}

	private void init() {
		initializeRelationalSchemas();
		generateRDBs();
	}

	private void initializeRelationalSchemas() {
		RelationalSchemaGenerator schemaGenerator = new RelationalSchemaGenerator(RelationalSchemaConfig.createDefault());
		
		List<CollectSurvey> surveys = surveyManager.getAll();
		for (CollectSurvey survey : surveys) {
			// Generate relational model
			try {
				RelationalSchema relationalSchema = schemaGenerator.generateSchema(survey, survey.getName());
				surveyIdToRelationalSchema.put(survey.getId(), relationalSchema);
			} catch(CollectRdbException e) {
				LOG.error("Error generating relational schema for survey " + survey.getName(), e);
			}
		}
	}
	
	private void generateRDBs() {
		List<CollectSurvey> surveys = surveyManager.getAll();
		for (CollectSurvey survey : surveys) {
			for (Step step : Step.values()) {
				if (! localRDBStorageManager.existsRDBFile(survey, step)) {
					try {
						generateRDB(survey, step);
					} catch(CollectRdbException e) {
						LOG.error("Error generating RDB for survey " + survey.getName(), e);
					}
				}
			}
		}
	}
	
	private void generateRDB(CollectSurvey survey, Step step) throws CollectRdbException {
		RelationalSchema relationalSchema = surveyIdToRelationalSchema.get(survey.getId());
		
		Connection targetConn = createTargetConnection(survey, step);
		
		RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		relationalSchemaCreator.createRelationalSchema(relationalSchema, targetConn);
		
		// Insert data
		RecordFilter recordFilter = new RecordFilter(survey);
		recordFilter.setStep(step);
		
		List<CollectRecord> summaries = recordManager.loadSummaries(recordFilter);
		insertRecords(survey, summaries, step, relationalSchema, targetConn);
	}
	
	private Connection createTargetConnection(CollectSurvey survey, Step step) throws CollectRdbException {
		try {
			File rdbFile = localRDBStorageManager.getRDBFile(survey, step);
			String pathToDbFile = rdbFile.getAbsolutePath();
			String connectionUrl = "jdbc:sqlite:" + pathToDbFile;
			Class.forName(SQLITE_DRIVER_CLASS_NAME);
			Connection c = DriverManager.getConnection(connectionUrl);
			return c;
		} catch (Exception e) {
			throw new CollectRdbException(String.format("Error creating connection to RDB for survey %s", survey.getName()), e);
		}
	}

	private void insertRecords(CollectSurvey survey, List<CollectRecord> summaries, Step step, 
			RelationalSchema targetSchema, Connection targetConn) throws CollectRdbException {
		DatabaseExporter databaseUpdater = createRDBUpdater(targetConn);
		databaseUpdater.insertReferenceData(targetSchema);
		for (int i = 0; i < summaries.size(); i++) {
			CollectRecord summary = summaries.get(i);
			CollectRecord record = recordManager.load(survey, summary.getId(), step);
			databaseUpdater.insertData(targetSchema, record);
		}
		try {
			targetConn.commit();
		} catch (SQLException e) {
			throw new CollectRdbException(String.format("Error inserting records related to survey %s into RDB", survey.getName()), e);
		}
	}

	@Override
	public void onEvents(List<? extends RecordEvent> events) {
		Step entry = Step.ENTRY;
		RelationalSchema rdbSchema = getRelatedRelationalSchema(events);
		CollectSurvey survey = (CollectSurvey) rdbSchema.getSurvey();
		try {
			Connection rdbConnection = createTargetConnection(survey, entry);
			JooqDatabaseExporter rdbUpdater = createRDBUpdater(rdbConnection);
			boolean notProcessedEvents = false;
			Integer lastRecordId = null;
			for (RecordEvent recordEvent : events) {
				Integer recordId = recordEvent.getRecordId();
				if (notProcessedEvents && ! lastRecordId.equals(recordId)) {
					udpateRecordData(rdbSchema, rdbUpdater, lastRecordId);
					notProcessedEvents = false;
				} else if (recordEvent instanceof RecordDeletedEvent) {
					deleteRecordData(rdbSchema, rdbUpdater, recordId);
				} else {
					notProcessedEvents = true;
				}
				lastRecordId = recordId;
			}
			if (notProcessedEvents) {
				udpateRecordData(rdbSchema, rdbUpdater, lastRecordId);
			}
			rdbConnection.commit();
		} catch (Exception e) {
			LOG.error("Error processing record events: " + e.getMessage(), e);
		}
	}

	private void deleteRecordData(RelationalSchema rdbSchema, JooqDatabaseExporter rdbUpdater,
			Integer recordId) throws CollectRdbException {
		CollectSurvey survey = (CollectSurvey) rdbSchema.getSurvey();
		CollectRecord record = recordManager.load(survey, recordId);
		rdbUpdater.deleteData(rdbSchema, record);
	}

	private void udpateRecordData(RelationalSchema rdbSchema, JooqDatabaseExporter rdbUpdater,
			Integer recordId) throws CollectRdbException {
		CollectSurvey survey = (CollectSurvey) rdbSchema.getSurvey();
		CollectRecord record = recordManager.load(survey, recordId);
		rdbUpdater.updateData(rdbSchema, record);
	}

	private RelationalSchema getRelatedRelationalSchema(
			List<? extends RecordEvent> events) {
		for (RecordEvent event : events) {
			String surveyName = event.getSurveyName();
			CollectSurvey survey = surveyManager.get(surveyName);
			if (survey != null) {
				return surveyIdToRelationalSchema.get(survey.getId());
			}
		}
		return null;
	}
	
	private JooqDatabaseExporter createRDBUpdater(Connection targetConn) {
		return new JooqDatabaseExporter(targetConn);
	}
	
}
