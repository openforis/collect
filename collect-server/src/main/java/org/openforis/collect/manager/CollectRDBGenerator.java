package org.openforis.collect.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.event.EventListener;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
//@Lazy(false)
public class CollectRDBGenerator implements EventListener {

	private static final Log LOG = LogFactory.getLog(CollectRDBGenerator.class);
	private static final String SQLITE_DRIVER_CLASS_NAME = "org.sqlite.JDBC";

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private CollectLocalRDBStorageManager localRDBStorageManager;
	
	private Map<Integer, RelationalSchema> surveyIdToRelationalSchema;
	
	public CollectRDBGenerator() {
		surveyIdToRelationalSchema = new HashMap<Integer, RelationalSchema>();
	}
	
	@PostConstruct
	public void init() {
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
		DatabaseExporter databaseExporter = new JooqDatabaseExporter(targetConn);
		databaseExporter.insertReferenceData(targetSchema);
		for (int i = 0; i < summaries.size(); i++) {
			CollectRecord summary = summaries.get(i);
			CollectRecord record = recordManager.load(survey, summary.getId(), step);
			databaseExporter.insertData(targetSchema, record);
		}
		try {
			targetConn.commit();
		} catch (SQLException e) {
			throw new CollectRdbException(String.format("Error inserting records related to survey %s into RDB", survey.getName()), e);
		}
	}
	
	@Override
	public void onEvents(List<? extends RecordEvent> events) {
		
	}

}
