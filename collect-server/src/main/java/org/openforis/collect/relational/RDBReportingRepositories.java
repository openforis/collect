package org.openforis.collect.relational;

import java.io.File;
import java.io.IOException;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
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
import org.openforis.collect.io.metadata.collectearth.NewMondrianSchemaGenerator;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.relational.data.ColumnValuePair;
import org.openforis.collect.relational.data.internal.DataTableDataExtractor;
import org.openforis.collect.relational.jooq.JooqDatabaseExporter;
import org.openforis.collect.relational.jooq.JooqRelationalSchemaCreator;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.reporting.MondrianSchemaStorageManager;
import org.openforis.collect.reporting.ReportingRepositories;
import org.openforis.collect.reporting.ReportingRepositoryInfo;
import org.openforis.collect.reporting.SaikuDatasourceStorageManager;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.concurrency.ProcessProgressListener;
import org.openforis.concurrency.ProcessStepProgressListener;
import org.openforis.concurrency.Progress;
import org.openforis.concurrency.ProgressListener;
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
	private MondrianSchemaStorageManager mondrianSchemaStorageManager;
	private SaikuDatasourceStorageManager saikuDatasourceStorageManager;
	
	private RelationalSchemaConfig rdbConfig = RelationalSchemaConfig.createDefault();
	private Map<String, RelationalSchema> relationalSchemaDefinitionBySurvey;
	private Map<String, String> mondrianSchemaDefinitionBySurvey;
	
	public RDBReportingRepositories(SurveyManager surveyManager, RecordManager recordManager, 
			CollectLocalRDBStorageManager localRDBStorageManager, MondrianSchemaStorageManager mondrianSchemaStorageManager,
			SaikuDatasourceStorageManager saikuDatasourceStorageManager) {
		this.surveyManager = surveyManager;
		this.recordManager = recordManager;
		this.localRDBStorageManager = localRDBStorageManager;
		this.mondrianSchemaStorageManager = mondrianSchemaStorageManager;
		this.saikuDatasourceStorageManager = saikuDatasourceStorageManager;
		this.relationalSchemaDefinitionBySurvey = new HashMap<String, RelationalSchema>();
		this.mondrianSchemaDefinitionBySurvey = new HashMap<String, String>();
	}

	public void init() {
		initializeSchemaDefinitions();
	}

	private void initializeSchemaDefinitions() {
		List<CollectSurvey> surveys = surveyManager.getAll();
		for (CollectSurvey survey : surveys) {
			initializeRelationalSchemaDefinition(survey);
			initializeMondrianSchemaDefinition(survey, survey.getDefaultLanguage());
		}
	}
	
	@Override
	public void createRepositories(String surveyName, String preferredLanguage, ProgressListener progressListener) {
		initializeRelationalSchemaDefinition(surveyName);
		ProcessProgressListener processProgressListener = new ProcessProgressListener(RecordStep.values().length);
		for (RecordStep step : RecordStep.values()) {
			try {
				createRepository(surveyName, step, preferredLanguage, new ProcessStepProgressListener(processProgressListener, progressListener));
				processProgressListener.stepCompleted();
			} catch(CollectRdbException e) {
				LOG.error("Error generating RDB for survey " + surveyName, e);
			}
		}
		updateMondrianSchemaFile(surveyName, preferredLanguage);
		writeSaikuDatasources(surveyName);
	}

	@Override
	public void createRepository(final String surveyName, final RecordStep recordStep, final String preferredLanguage, final ProgressListener progressListener) {
		localRDBStorageManager.deleteRDBFile(surveyName, recordStep);
		
		updateMondrianSchemaFile(surveyName, preferredLanguage);
		if (saikuDatasourceStorageManager.isSaikuAvailable()) {
			writeSaikuDatasource(surveyName, recordStep);
		}
		
		final RelationalSchema relationalSchema = getOrInitializeRelationalSchemaDefinition(surveyName);
		
		withConnection(surveyName, recordStep, new Callback() {
			public void execute(Connection connection) {
				RelationalSchemaCreator relationalSchemaCreator = new JooqRelationalSchemaCreator();
				relationalSchemaCreator.createRelationalSchema(relationalSchema, connection);
				insertRecords(surveyName, recordStep, relationalSchema, connection, progressListener);
				relationalSchemaCreator.addConstraints(relationalSchema, connection);
				relationalSchemaCreator.addIndexes(relationalSchema, connection);
			}
		});
	}

	private void updateMondrianSchemaFile(String surveyName, String preferredLanguage) {
		CollectSurvey survey = surveyManager.get(surveyName);
		initializeMondrianSchemaDefinition(survey, preferredLanguage);
		writeMondrianSchemaFile(surveyName);
	}

	private void writeSaikuDatasources(String surveyName) {
		if (saikuDatasourceStorageManager.isSaikuAvailable()) {
			for (RecordStep recordStep : RecordStep.values()) {
				writeSaikuDatasource(surveyName, recordStep);
			}
		}
	}

	private void writeSaikuDatasource(String surveyName, RecordStep recordStep) {
		saikuDatasourceStorageManager.writeDatasourceFile(surveyName, recordStep);
	}
	
	private void deleteSaikuDatasources(String surveyName) {
		if (saikuDatasourceStorageManager.isSaikuAvailable()) {
			for (RecordStep recordStep : RecordStep.values()) {
				saikuDatasourceStorageManager.deleteDatasourceFile(surveyName, recordStep);
			}
		}
	}

	private void writeMondrianSchemaFile(String surveyName) {
		try {
			mondrianSchemaStorageManager.createBackupCopy(surveyName);
			File file = mondrianSchemaStorageManager.getSchemaFile(surveyName);
			String schema = mondrianSchemaDefinitionBySurvey.get(surveyName);
			FileUtils.write(file, schema, OpenForisIOUtils.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Error generating mondrian schema for survey : "+ surveyName, e);
		}
	}

	private void insertRecords(String surveyName, RecordStep recordStep, 
			RelationalSchema targetSchema, Connection targetConn,
			ProgressListener progressListener) throws CollectRdbException {
		ProcessProgressListener processProgressListener = new ProcessProgressListener(2);
		CollectSurvey survey = surveyManager.get(surveyName);
		DatabaseExporter databaseUpdater = createRDBUpdater(targetSchema, targetConn);
		
		databaseUpdater.insertReferenceData(new ProcessStepProgressListener(processProgressListener, progressListener));
		processProgressListener.stepCompleted();
		
		RecordFilter recordFilter = new RecordFilter(survey);
		Step step = Step.fromRecordStep(recordStep);
		recordFilter.setStepGreaterOrEqual(step);
		List<CollectRecord> summaries = recordManager.loadSummaries(recordFilter);
		
		ProcessStepProgressListener recordInsertProcessListener = new ProcessStepProgressListener(processProgressListener, progressListener);
		recordInsertProcessListener.progressMade(new Progress(0, summaries.size()));
		
		long processedRecords = 0;
		for (CollectRecord summary : summaries) {
			CollectRecord record = recordManager.load(survey, summary.getId(), step, false);
			databaseUpdater.insertRecordData(record, ProgressListener.NULL_PROGRESS_LISTENER);
			processedRecords++;
			recordInsertProcessListener.progressMade(new Progress(processedRecords, summaries.size()));
		}
		IOUtils.closeQuietly(databaseUpdater);
		processProgressListener.stepCompleted();
	}

	@Override
	public void updateRepositories(String surveyName, String preferredLanguage, ProgressListener progressListener) {
		createRepositories(surveyName, preferredLanguage, progressListener);
	}

	@Override
	public void deleteRepositories(String surveyName) {
		for (RecordStep step : RecordStep.values()) {
			localRDBStorageManager.deleteRDBFile(surveyName, step);
		}
		relationalSchemaDefinitionBySurvey.remove(surveyName);
		mondrianSchemaStorageManager.deleteSchemaFile(surveyName);
		deleteSaikuDatasources(surveyName);			
	}

	@Override
	public void process(final RecordTransaction recordTransaction) {
		final RelationalSchema rdbSchema = getRelatedRelationalSchema(recordTransaction);
		final CollectSurvey survey = (CollectSurvey) rdbSchema.getSurvey();
		RecordStep step = recordTransaction.getRecordStep();

		withConnection(survey.getName(), step, new Callback() {
			public void execute(Connection connection) {
				RDBUpdater rdbUpdater = createRDBUpdater(rdbSchema, connection);
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
			result.add(getRepositoryPath(surveyName, recordStep));
		}
		return result;
	}

	@Override
	public String getRepositoryPath(String surveyName, RecordStep recordStep) {
		File rdbFile = localRDBStorageManager.getRDBFile(surveyName, recordStep);
		String path = rdbFile.getAbsolutePath();
		return path;
	}
	
	@Override
	public ReportingRepositoryInfo getInfo(String surveyName) {
		Date rdbFileDate = localRDBStorageManager.getRDBFileDate(surveyName, RecordStep.ENTRY);
		if (rdbFileDate == null) {
			return null;
		} else {
			ReportingRepositoryInfo info = new ReportingRepositoryInfo();
			info.setLastUpdate(rdbFileDate);
			RecordFilter filter = new RecordFilter(surveyManager.get(surveyName));
			filter.setModifiedSince(rdbFileDate);
			info.setUpdatedRecordsSinceLastUpdate(recordManager.countRecords(filter));
			return info;
		}
	}

	private RelationalSchema getOrInitializeRelationalSchemaDefinition(
			final String surveyName) {
		if (! relationalSchemaDefinitionBySurvey.containsKey(surveyName)) {
			initializeRelationalSchemaDefinition(surveyName);
		}
		return relationalSchemaDefinitionBySurvey.get(surveyName);
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
			String pathToDbFile = getRepositoryPath(surveyName, step);
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
		return survey == null ? null : relationalSchemaDefinitionBySurvey.get(survey.getName());
	}
	
	private void initializeRelationalSchemaDefinition(String surveyName) {
		initializeRelationalSchemaDefinition(surveyManager.get(surveyName));
	}
	
	private void initializeRelationalSchemaDefinition(CollectSurvey survey) {
		try {
			RelationalSchemaGenerator schemaGenerator = new RelationalSchemaGenerator(rdbConfig);
			RelationalSchema relationalSchema = schemaGenerator.generateSchema(survey, survey.getName());
			relationalSchemaDefinitionBySurvey.put(survey.getName(), relationalSchema);
		} catch(CollectRdbException e) {
			LOG.error("Error generating relational schema for survey " + survey.getName(), e);
		}
	}
	
	private void initializeMondrianSchemaDefinition(CollectSurvey survey, String preferredLanguage) {
		try {
			boolean schemaLess = true; //TODO
			String schemaName = schemaLess ? "" : survey.getName();
			NewMondrianSchemaGenerator schemaGenerator = new NewMondrianSchemaGenerator(survey, 
					ObjectUtils.defaultIfNull(preferredLanguage, survey.getDefaultLanguage()), 
					schemaName, rdbConfig);
			String mondrianSchema = schemaGenerator.generateXMLSchema();
			mondrianSchemaDefinitionBySurvey.put(survey.getName(), mondrianSchema);
		} catch(CollectRdbException e) {
			LOG.error("Error generating relational schema for survey " + survey.getName(), e);
		}
	}
	
	private JooqDatabaseExporter createRDBUpdater(RelationalSchema schema, Connection targetConn) {
		return new JooqDatabaseExporter(schema, targetConn);
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
			rdbUpdater.insertEntity(recordEvent.getRecordId(), getParentEntityId(), 
					getNodeId(), getDefinitionId());
		}
		
		private void insertAttribute() {
			rdbUpdater.insertAttribute(recordEvent.getRecordId(), getParentEntityId(), 
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
			rdbUpdater.updateEntityData(dataTable, pkValue, columnValuePairs);
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
			rdbUpdater.deleteEntity(recordEvent.getRecordId(), getNodeId(), getDefinitionId());
		}
		
		private void deleteAttribute() {
			rdbUpdater.deleteAttribute(recordEvent.getRecordId(), getNodeId(), getDefinitionId());
		}

		private void deleteRecord() {
			rdbUpdater.deleteRecordData(recordEvent.getRecordId(), getDefinitionId());
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
