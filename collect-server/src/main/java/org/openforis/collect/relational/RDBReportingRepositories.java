package org.openforis.collect.relational;

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
import org.openforis.collect.event.AttributeCreatedEvent;
import org.openforis.collect.event.AttributeEvent;
import org.openforis.collect.event.AttributeUpdatedEvent;
import org.openforis.collect.event.BooleanAttributeUpdatedEvent;
import org.openforis.collect.event.CodeAttributeUpdatedEvent;
import org.openforis.collect.event.CoordinateAttributeUpdatedEvent;
import org.openforis.collect.event.DateAttributeUpdatedEvent;
import org.openforis.collect.event.EntityCreatedEvent;
import org.openforis.collect.event.EntityDeletedEvent;
import org.openforis.collect.event.NumberAttributeUpdatedEvent;
import org.openforis.collect.event.NumericAttributeUpdatedEvent;
import org.openforis.collect.event.RangeAttributeUpdatedEvent;
import org.openforis.collect.event.RecordDeletedEvent;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.event.RecordStep;
import org.openforis.collect.event.RecordTransaction;
import org.openforis.collect.event.TaxonAttributeUpdatedEvent;
import org.openforis.collect.event.TextAttributeUpdatedEvent;
import org.openforis.collect.event.TimeAttributeUpdatedEvent;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
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
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class RDBReportingRepositories implements ReportingRepositories {

	private static final Log LOG = LogFactory.getLog(RDBReportingRepositories.class);
	private static final String SQLITE_DRIVER_CLASS_NAME = "org.sqlite.JDBC";

	private SurveyManager surveyManager;
	private RecordManager recordManager;
	private CollectLocalRDBStorageManager localRDBStorageManager;
	
	private Map<String, RelationalSchema> relationalSchemaDefinitionBySurveyName;
	
	public RDBReportingRepositories(SurveyManager surveyManager, RecordManager recordManager, 
			CollectLocalRDBStorageManager localRDBStorageManager) {
		this.surveyManager = surveyManager;
		this.recordManager = recordManager;
		this.localRDBStorageManager = localRDBStorageManager;
		this.relationalSchemaDefinitionBySurveyName = new HashMap<String, RelationalSchema>();
	}

	public void init() {
		initializeRelationalSchemaDefinitions();
	}

	private void initializeRelationalSchemaDefinitions() {
		List<CollectSurvey> surveys = surveyManager.getAll();
		for (CollectSurvey survey : surveys) {
			initializeRelationSchemaDefinition(survey);
		}
	}
	
	@Override
	public void createRepositories(String surveyName) {
		initializeRelationalSchemaDefinition(surveyName);
		for (RecordStep step : RecordStep.values()) {
			try {
				createRepository(surveyName, step);
			} catch(CollectRdbException e) {
				LOG.error("Error generating RDB for survey " + surveyName, e);
			}
		}
	}

	@Override
	public void createRepository(final String surveyName, final RecordStep recordStep) {
		deleteRDB(surveyName, recordStep);
		
		final RelationalSchema relationalSchema = getOrInitializeRelationalSchemaDefinition(surveyName);
		
		withConnection(surveyName, recordStep, new Callback() {
			public void execute(Connection connection) {
				RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
				relationalSchemaCreator.createRelationalSchema(relationalSchema, connection);
				
				insertRecords(surveyName, recordStep, relationalSchema, connection);
			}
		});
	}

	private void insertRecords(String surveyName, RecordStep recordStep, 
			RelationalSchema targetSchema, Connection targetConn) throws CollectRdbException {
		CollectSurvey survey = surveyManager.get(surveyName);
		DatabaseExporter databaseUpdater = createRDBUpdater(targetConn);
		databaseUpdater.insertReferenceData(targetSchema);
		RecordFilter recordFilter = new RecordFilter(survey);
		Step step = Step.fromRecordStep(recordStep);
		recordFilter.setStep(step);
		List<CollectRecord> summaries = recordManager.loadSummaries(recordFilter);
		for (int i = 0; i < summaries.size(); i++) {
			CollectRecord summary = summaries.get(i);
			CollectRecord record = recordManager.load(survey, summary.getId(), step);
			databaseUpdater.insertData(targetSchema, record);
		}
	}

	@Override
	public void updateRepositories(String surveyName) {
		createRepositories(surveyName);
	}

	@Override
	public void deleteRepositories(String surveyName) {
		for (RecordStep step : RecordStep.values()) {
			deleteRDB(surveyName, step);
		}
		relationalSchemaDefinitionBySurveyName.remove(surveyName);
	}

	@Override
	public void process(final RecordTransaction recordTransaction) {
		final RelationalSchema rdbSchema = getRelatedRelationalSchema(recordTransaction);
		final CollectSurvey survey = (CollectSurvey) rdbSchema.getSurvey();
		RecordStep step = recordTransaction.getRecordStep();

		withConnection(survey.getName(), step, new Callback() {
			public void execute(Connection connection) {
				RDBUpdater rdbUpdater = createRDBUpdater(connection);
				for (RecordEvent recordEvent : recordTransaction.getEvents()) {
					EventHandler handler = new EventHandler(recordEvent, rdbSchema, survey, rdbUpdater);
					handler.handle();
				}
			}
		});
	}
	
	@Override
	public List<String> getRepositoryPaths(String surveyName) {
		List<String> result = new ArrayList<String>();
		for (RecordStep recordStep : RecordStep.values()) {
			File rdbFile = localRDBStorageManager.getRDBFile(surveyName, recordStep);
			result.add(rdbFile.getAbsolutePath());
		}
		return result;
	}

	private void deleteRDB(String surveyName, RecordStep step) {
		File rdbFile = localRDBStorageManager.getRDBFile(surveyName, step);
		if (rdbFile != null && rdbFile.exists()) {
			rdbFile.delete();
		}
	}
	
	private RelationalSchema getOrInitializeRelationalSchemaDefinition(
			final String surveyName) {
		if (! relationalSchemaDefinitionBySurveyName.containsKey(surveyName)) {
			initializeRelationalSchemaDefinition(surveyName);
		}
		final RelationalSchema relationalSchema = relationalSchemaDefinitionBySurveyName.get(surveyName);
		return relationalSchema;
	}

	private void withConnection(String surveyName, RecordStep recordStep, Callback job) {
		Connection connection = null;
		try {
			connection = createTargetConnection(surveyName, recordStep);
			connection.setAutoCommit(false);
			job.execute(connection);
			connection.commit();
		} catch (Exception e) {
			LOG.error("Error processing record events: " + e.getMessage(), e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				LOG.error("Rollback failed: " + e1.getMessage(), e1);
			}
			throw new RuntimeException("Error processing RDB generation events", e);
		} finally {
			try {
				if (connection != null) { 
					connection.close();
				}
			} catch (SQLException e) {}
		}
	}

	private Connection createTargetConnection(String surveyName, RecordStep step) throws CollectRdbException {
		try {
			File rdbFile = localRDBStorageManager.getRDBFile(surveyName, step);
			String pathToDbFile = rdbFile.getAbsolutePath();
			String connectionUrl = "jdbc:sqlite:" + pathToDbFile;
			Class.forName(SQLITE_DRIVER_CLASS_NAME);
			Connection c = DriverManager.getConnection(connectionUrl);
			return c;
		} catch (Exception e) {
			throw new CollectRdbException(String.format("Error creating connection to RDB for survey %s", surveyName), e);
		}
	}

	private RelationalSchema getRelatedRelationalSchema(
			RecordTransaction recordTransaction) {
		String surveyName = recordTransaction.getSurveyName();
		CollectSurvey survey = surveyManager.get(surveyName);
		return survey == null ? null : relationalSchemaDefinitionBySurveyName.get(survey.getName());
	}
	
	private void initializeRelationalSchemaDefinition(final String surveyName) {
		CollectSurvey survey = surveyManager.get(surveyName);
		initializeRelationSchemaDefinition(survey);
	}
	
	private void initializeRelationSchemaDefinition(CollectSurvey survey) {
		// Generate relational model
		try {
			RelationalSchemaGenerator schemaGenerator = new RelationalSchemaGenerator(RelationalSchemaConfig.createDefault());
			RelationalSchema relationalSchema = schemaGenerator.generateSchema(survey, survey.getName());
			relationalSchemaDefinitionBySurveyName.put(survey.getName(), relationalSchema);
		} catch(CollectRdbException e) {
			LOG.error("Error generating relational schema for survey " + survey.getName(), e);
		}
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
			} else if (recordEvent instanceof AttributeEvent) {
				if (recordEvent instanceof AttributeCreatedEvent) {
					insertAttribute();
				} else if (recordEvent instanceof AttributeUpdatedEvent) {
					updateAttributeData();
				} else {
					deleteAttribute();
				}
			} else if (recordEvent instanceof EntityDeletedEvent) {
				deleteEntity();
			} else if (recordEvent instanceof RecordDeletedEvent) {
				deleteRecord();
			}
		}

		private void insertEntity() {
			rdbUpdater.insertEntity(rdbSchema, recordEvent.getRecordId(), getParentEntityId(), 
					getNodeId(), getDefinitionId());
		}
		
		private void insertAttribute() {
			rdbUpdater.insertAttribute(rdbSchema, recordEvent.getRecordId(), getParentEntityId(), 
					getNodeId(), getDefinitionId());
		}

		private void updateAttributeData() {
			AttributeDefinition def = (AttributeDefinition) survey.getSchema().getDefinitionById(getDefinitionId());
			Integer rowNodeId;
			DataTable dataTable;
			if (def.isMultiple()) {
				dataTable = rdbSchema.getDataTable(def);
				rowNodeId = getNodeId();
			} else {
				EntityDefinition multipleEntityDef = def.getNearestAncestorMultipleEntity();
				dataTable = rdbSchema.getDataTable(multipleEntityDef);
				rowNodeId = getParentEntityId();
			}
			NodeDefinition tableNodeDef = dataTable.getNodeDefinition();

			BigInteger pkValue = DataTableDataExtractor.getTableArtificialPK(recordEvent.getRecordId(), tableNodeDef, rowNodeId);
			List<ColumnValuePair<DataColumn, ?>> columnValuePairs = toColumnValuePairs(dataTable, def);
			rdbUpdater.updateData(rdbSchema, dataTable, pkValue, columnValuePairs);
		}

		@SuppressWarnings("unchecked")
		public List<ColumnValuePair<DataColumn, ?>> toColumnValuePairs(
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
			rdbUpdater.deleteEntity(rdbSchema, recordEvent.getRecordId(), getNodeId(), getDefinitionId());
		}
		
		private void deleteAttribute() {
			rdbUpdater.deleteAttribute(rdbSchema, recordEvent.getRecordId(), getNodeId(), getDefinitionId());
		}

		private void deleteRecord() {
			rdbUpdater.deleteData(rdbSchema, recordEvent.getRecordId(), getDefinitionId());
		}
		
		private Integer getParentEntityId() {
			String definitionId = recordEvent.getDefinitionId();
			NodeDefinition def = survey.getSchema().getDefinitionById(Integer.parseInt(definitionId));
			boolean isRootEntity = def instanceof EntityDefinition && ((EntityDefinition) def).isRoot();
			if (isRootEntity) {
				return null;
			}
			boolean inCollection = def.isMultiple();
			int parentIdx = inCollection ? 1 : 0;
			List<String> ancestorIds = recordEvent.getAncestorIds();
			return Integer.parseInt(ancestorIds.get(parentIdx));
		}

		private int getNodeId() {
			return Integer.parseInt(recordEvent.getNodeId());
		}

		private int getDefinitionId() {
			return Integer.parseInt(recordEvent.getDefinitionId());
		}

	}

}
