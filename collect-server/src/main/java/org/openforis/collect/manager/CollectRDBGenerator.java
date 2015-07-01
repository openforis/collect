package org.openforis.collect.manager;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.event.AttributeUpdatedEvent;
import org.openforis.collect.event.BooleanAttributeUpdatedEvent;
import org.openforis.collect.event.CodeAttributeUpdatedEvent;
import org.openforis.collect.event.DateAttributeUpdatedEvent;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.NumberAttributeUpdatedEvent;
import org.openforis.collect.event.NumericAttributeUpdatedEvent;
import org.openforis.collect.event.RangeAttributeUpdatedEvent;
import org.openforis.collect.event.RecordDeletedEvent;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.event.TaxonAttributeUpdatedEvent;
import org.openforis.collect.event.TextAttributeUpdatedEvent;
import org.openforis.collect.event.TimeAttributeUpdatedEvent;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.relational.CollectLocalRDBStorageManager;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.RDBUpdater;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.data.ColumnValuePair;
import org.openforis.collect.relational.data.internal.DataTableDataExtractor;
import org.openforis.collect.relational.jooq.JooqDatabaseExporter;
import org.openforis.collect.relational.liquibase.LiquibaseRelationalSchemaCreator;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;

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
				if (recordEvent instanceof AttributeUpdatedEvent) {
					updateAttributeData(rdbSchema, rdbUpdater, (AttributeUpdatedEvent) recordEvent);
				}
				
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

	private void updateAttributeData(RelationalSchema rdbSchema,
			RDBUpdater rdbUpdater, AttributeUpdatedEvent recordEvent) {
		CollectSurvey survey = (CollectSurvey) rdbSchema.getSurvey();
		AttributeDefinition def = (AttributeDefinition) survey.getSchema().getDefinitionById(recordEvent.getDefinitionId());
		EntityDefinition multipleEntityDef = def.getNearestAncestorMultipleEntity();
		BigInteger pkValue = DataTableDataExtractor.getArtificialPK(recordEvent.getRecordId(), multipleEntityDef, recordEvent.getParentEntityId());
		DataTable dataTable = rdbSchema.getDataTable(multipleEntityDef);
		List<ColumnValuePair<DataColumn, ?>> columnValuePairs = toColumnValueParis(dataTable, def, recordEvent);
		rdbUpdater.updateData(rdbSchema, dataTable, pkValue, columnValuePairs);
	}

	@SuppressWarnings("unchecked")
	public List<ColumnValuePair<DataColumn, ?>> toColumnValueParis(
			DataTable dataTable, AttributeDefinition attributeDef,
			AttributeUpdatedEvent recordEvent) {
		List<DataColumn> dataColumns = dataTable.getDataColumns(attributeDef);
		List<ColumnValuePair<DataColumn, ?>> columnValuePairs = new ArrayList<ColumnValuePair<DataColumn, ?>>();
		if (recordEvent instanceof BooleanAttributeUpdatedEvent) {
			columnValuePairs.add(new ColumnValuePair<DataColumn, Boolean>(dataColumns.get(0), ((BooleanAttributeUpdatedEvent) recordEvent).getValue()));
		} else if (recordEvent instanceof CodeAttributeUpdatedEvent) {
			CodeAttributeUpdatedEvent evt = (CodeAttributeUpdatedEvent) recordEvent;
			CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) attributeDef;
			DataColumn codeColumn = dataTable.getDataColumn(codeAttrDef.getCodeFieldDefinition());
			DataColumn qualifierColumn = dataTable.getDataColumn(codeAttrDef.getQualifierFieldDefinition());
			columnValuePairs = Arrays.<ColumnValuePair<DataColumn, ?>>asList(
					new ColumnValuePair<DataColumn, String>(codeColumn, evt.getCode()),
					new ColumnValuePair<DataColumn, String>(qualifierColumn, evt.getQualifier())
			);
		} else if (recordEvent instanceof DateAttributeUpdatedEvent) {
			columnValuePairs.add(new ColumnValuePair<DataColumn, Date>(dataColumns.get(0), ((DateAttributeUpdatedEvent) recordEvent).getDate()));
		} else if (recordEvent instanceof NumericAttributeUpdatedEvent) {
			NumericAttributeUpdatedEvent<?> numericAttributeUpdatedEvent = (NumericAttributeUpdatedEvent<?>) recordEvent;
			NumberAttributeDefinition numberAttrDef = (NumberAttributeDefinition) attributeDef;
			FieldDefinition<Integer> unitIdFieldDef = numberAttrDef.getUnitIdFieldDefinition();
			DataColumn unitColumn = dataTable.getDataColumn(unitIdFieldDef);
			columnValuePairs.add(new ColumnValuePair<DataColumn, Integer>(unitColumn, numericAttributeUpdatedEvent.getUnitId()));
			if (recordEvent instanceof NumberAttributeUpdatedEvent) {
				DataColumn valueColumn = dataTable.getDataColumn(((NumberAttributeDefinition) attributeDef).getValueFieldDefinition());
				Number value = ((NumberAttributeUpdatedEvent<?>) recordEvent).getValue();
				columnValuePairs.add(new ColumnValuePair<DataColumn, Number>(valueColumn, value));
			} else if (recordEvent instanceof RangeAttributeUpdatedEvent) {
				Number from = ((RangeAttributeUpdatedEvent<Number>) recordEvent).getFrom();
				DataColumn fromColumn = dataTable.getDataColumn(((RangeAttributeDefinition) attributeDef).getFromFieldDefinition());
				columnValuePairs.add(new ColumnValuePair<DataColumn, Number>(fromColumn, from));
				Number to = ((RangeAttributeUpdatedEvent<Number>) recordEvent).getTo();
				DataColumn toColumn = dataTable.getDataColumn(((RangeAttributeDefinition) attributeDef).getToFieldDefinition());
				columnValuePairs.add(new ColumnValuePair<DataColumn, Number>(toColumn, to));
			}
		} else if (recordEvent instanceof TaxonAttributeUpdatedEvent) {
			TaxonAttributeUpdatedEvent evt = (TaxonAttributeUpdatedEvent) recordEvent;
			TaxonAttributeDefinition taxonAttrDef = (TaxonAttributeDefinition) attributeDef;
			DataColumn codeColumn = dataTable.getDataColumn(taxonAttrDef.getCodeFieldDefinition());
			DataColumn scientificNameColumn = dataTable.getDataColumn(taxonAttrDef.getScientificNameFieldDefinition());
			DataColumn vernacularNameColumn = dataTable.getDataColumn(taxonAttrDef.getVernacularNameFieldDefinition());
			DataColumn languageCodeColumn = dataTable.getDataColumn(taxonAttrDef.getLanguageCodeFieldDefinition());
			DataColumn languageVarietyColumn = dataTable.getDataColumn(taxonAttrDef.getLanguageVarietyFieldDefinition());
			columnValuePairs = Arrays.<ColumnValuePair<DataColumn, ?>>asList(
					new ColumnValuePair<DataColumn, String>(codeColumn, evt.getCode()),
					new ColumnValuePair<DataColumn, String>(scientificNameColumn, evt.getScientificName()),
					new ColumnValuePair<DataColumn, String>(vernacularNameColumn, evt.getVernacularName()),
					new ColumnValuePair<DataColumn, String>(languageCodeColumn, evt.getLanguageCode()),
					new ColumnValuePair<DataColumn, String>(languageVarietyColumn, evt.getLanguageVariety())
			);
		} else if (recordEvent instanceof TextAttributeUpdatedEvent) {
			columnValuePairs.add(new ColumnValuePair<DataColumn, String>(dataColumns.get(0), ((TextAttributeUpdatedEvent) recordEvent).getText()));
		} else if (recordEvent instanceof TimeAttributeUpdatedEvent) {
			columnValuePairs.add(new ColumnValuePair<DataColumn, Date>(dataColumns.get(0), ((TimeAttributeUpdatedEvent) recordEvent).getTime()));
		} else {
			throw new UnsupportedOperationException("Unsupported record event type: " + recordEvent.getClass().getName());
		}
		return columnValuePairs;
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
