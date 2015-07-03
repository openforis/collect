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
import org.openforis.collect.event.CoordinateAttributeUpdatedEvent;
import org.openforis.collect.event.DateAttributeUpdatedEvent;
import org.openforis.collect.event.EntityCreatedEvent;
import org.openforis.collect.event.EntityDeletedEvent;
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
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
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
	
	private void generateRDB(final CollectSurvey survey, final Step recordStep) throws CollectRdbException {
		final RelationalSchema relationalSchema = surveyIdToRelationalSchema.get(survey.getId());
		
		withConnection(survey, recordStep, new Callback() {
			public void execute(Connection connection) {
				RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
				relationalSchemaCreator.createRelationalSchema(relationalSchema, connection);
				
				insertRecords(survey, recordStep, relationalSchema, connection);
			}
		});
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

	private void insertRecords(CollectSurvey survey, Step step, 
			RelationalSchema targetSchema, Connection targetConn) throws CollectRdbException {
		DatabaseExporter databaseUpdater = createRDBUpdater(targetConn);
		databaseUpdater.insertReferenceData(targetSchema);
		RecordFilter recordFilter = new RecordFilter(survey);
		recordFilter.setStep(step);
		List<CollectRecord> summaries = recordManager.loadSummaries(recordFilter);
		for (int i = 0; i < summaries.size(); i++) {
			CollectRecord summary = summaries.get(i);
			CollectRecord record = recordManager.load(survey, summary.getId(), step);
			databaseUpdater.insertData(targetSchema, record);
		}
	}

	@Override
	public void onEvents(final List<? extends RecordEvent> events) {
		Step recordStep = Step.ENTRY;
		final RelationalSchema rdbSchema = getRelatedRelationalSchema(events);
		final CollectSurvey survey = (CollectSurvey) rdbSchema.getSurvey();

		withConnection(survey, recordStep, new Callback() {
			public void execute(Connection connection) {
				RDBUpdater rdbUpdater = createRDBUpdater(connection);
				for (RecordEvent recordEvent : events) {
					EventHandler handler = new EventHandler(recordEvent, rdbSchema, survey, rdbUpdater);
					handler.handle();
				}
			}
		});
	}

	private void withConnection(CollectSurvey survey, Step recordStep, Callback job) {
		Connection connection = null;
		try {
			connection = createTargetConnection(survey, recordStep);
			connection.setAutoCommit(false);
			job.execute(connection);
			connection.commit();
		} catch (Exception e) {
			LOG.error("Error processing record events: " + e.getMessage(), e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				LOG.error("Rollback failed: " + e.getMessage(), e);
			}
		} finally {
			try {
				if (connection != null) { 
					connection.close();
				}
			} catch (SQLException e) {
			}
		}
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
	

	private interface Callback {
		
		void execute(Connection connection);
		
	}
	
	private class EventHandler {
		
		private RecordEvent recordEvent;
		private RelationalSchema rdbSchema;
		private CollectSurvey survey;
		private RDBUpdater rdbUpdater;
		
		public EventHandler(RecordEvent recordEvent,
				RelationalSchema rdbSchema, CollectSurvey survey, RDBUpdater rdbUpdater) {
			super();
			this.recordEvent = recordEvent;
			this.rdbSchema = rdbSchema;
			this.survey = survey;
			this.rdbUpdater = rdbUpdater;
		}
		
		public void handle() {
			if (recordEvent instanceof EntityCreatedEvent) {
				insertEntity();
			} else if (recordEvent instanceof AttributeUpdatedEvent) {
				updateAttributeData();
			} else if (recordEvent instanceof EntityDeletedEvent) {
				deleteEntity();
			} else if (recordEvent instanceof RecordDeletedEvent) {
				deleteRecord();
			}
		}

		private void insertEntity() {
			rdbUpdater.insertEntity(rdbSchema, recordEvent.getRecordId(), recordEvent.getParentEntityId(), recordEvent.getNodeId(), recordEvent.getDefinitionId());
		}

		private void updateAttributeData() {
			AttributeDefinition def = (AttributeDefinition) survey.getSchema().getDefinitionById(recordEvent.getDefinitionId());
			EntityDefinition multipleEntityDef = def.getNearestAncestorMultipleEntity();
			BigInteger pkValue = DataTableDataExtractor.getTableArtificialPK(recordEvent.getRecordId(), multipleEntityDef, recordEvent.getParentEntityId());
			DataTable dataTable = rdbSchema.getDataTable(multipleEntityDef);
			List<ColumnValuePair<DataColumn, ?>> columnValuePairs = toColumnValueParis(dataTable, def);
			rdbUpdater.updateData(rdbSchema, dataTable, pkValue, columnValuePairs);
		}

		@SuppressWarnings("unchecked")
		public List<ColumnValuePair<DataColumn, ?>> toColumnValueParis(
				DataTable dataTable, AttributeDefinition attributeDef) {
			List<DataColumn> dataColumns = dataTable.getDataColumns(attributeDef);
			List<ColumnValuePair<DataColumn, ?>> columnValuePairs = new ArrayList<ColumnValuePair<DataColumn, ?>>();
			if (recordEvent instanceof BooleanAttributeUpdatedEvent) {
				columnValuePairs.add(new ColumnValuePair<DataColumn, Boolean>(dataColumns.get(0), ((BooleanAttributeUpdatedEvent) recordEvent).getValue()));
			} else if (recordEvent instanceof CodeAttributeUpdatedEvent) {
				CodeAttributeUpdatedEvent evt = (CodeAttributeUpdatedEvent) recordEvent;
				CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) attributeDef;
				DataColumn codeColumn = dataTable.getDataColumn(codeAttrDef.getCodeFieldDefinition());
				DataColumn qualifierColumn = dataTable.getDataColumn(codeAttrDef.getQualifierFieldDefinition());
				columnValuePairs.add(new ColumnValuePair<DataColumn, String>(codeColumn, evt.getCode()));
				if (qualifierColumn != null) {
					columnValuePairs.add(new ColumnValuePair<DataColumn, String>(qualifierColumn, evt.getQualifier()));	
				}
			} else if (recordEvent instanceof CoordinateAttributeUpdatedEvent) {
				CoordinateAttributeDefinition coordinateAttrDef = (CoordinateAttributeDefinition) attributeDef;
				CoordinateAttributeUpdatedEvent e = (CoordinateAttributeUpdatedEvent) recordEvent;
				columnValuePairs = Arrays.<ColumnValuePair<DataColumn, ?>>asList(
						new ColumnValuePair<DataColumn, Double>(dataTable.getDataColumn(coordinateAttrDef.getXField()), e.getX()),
						new ColumnValuePair<DataColumn, Double>(dataTable.getDataColumn(coordinateAttrDef.getYField()), e.getX()),
						new ColumnValuePair<DataColumn, String>(dataTable.getDataColumn(coordinateAttrDef.getSrsIdField()), e.getSrsId())
				);
			} else if (recordEvent instanceof DateAttributeUpdatedEvent) {
				columnValuePairs.add(new ColumnValuePair<DataColumn, Date>(dataColumns.get(0), ((DateAttributeUpdatedEvent) recordEvent).getDate()));
			} else if (recordEvent instanceof NumericAttributeUpdatedEvent) {
				NumericAttributeUpdatedEvent<?> numericAttributeUpdatedEvent = (NumericAttributeUpdatedEvent<?>) recordEvent;
				NumberAttributeDefinition numberAttrDef = (NumberAttributeDefinition) attributeDef;
				FieldDefinition<Integer> unitIdFieldDef = numberAttrDef.getUnitIdFieldDefinition();
				DataColumn unitColumn = dataTable.getDataColumn(unitIdFieldDef);
				if (unitColumn != null) {
					columnValuePairs.add(new ColumnValuePair<DataColumn, Integer>(unitColumn, numericAttributeUpdatedEvent.getUnitId()));
				}
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

		private void deleteEntity() {
			rdbUpdater.deleteEntity(rdbSchema, recordEvent.getRecordId(), recordEvent.getNodeId(), recordEvent.getDefinitionId());
		}

		private void deleteRecord() {
			rdbUpdater.deleteData(rdbSchema, recordEvent.getRecordId(), recordEvent.getDefinitionId());
		}
		
	}
	
}
