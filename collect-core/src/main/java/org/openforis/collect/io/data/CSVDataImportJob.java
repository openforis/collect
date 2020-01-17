/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.ReferenceDataImportStatus;
import org.openforis.collect.io.data.DataLine.EntityIdentifier;
import org.openforis.collect.io.data.DataLine.EntityIdentifierDefinition;
import org.openforis.collect.io.data.DataLine.EntityKeysIdentifier;
import org.openforis.collect.io.data.DataLine.EntityPositionIdentifier;
import org.openforis.collect.io.data.DataLine.FieldValueKey;
import org.openforis.collect.io.data.csv.CSVDataExportParametersBase.OutputFormat;
import org.openforis.collect.io.data.csv.CSVDataImportSettings;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NodeAddChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeBatchProcessor;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.ZipFiles;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.Worker;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.AbstractValue;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component(CSVDataImportJob.BEAN_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CSVDataImportJob extends Job {

	public static final String BEAN_NAME = "csvDataImportJob";
	
	private CSVDataImportInput input;
	//transient
	private File tempInputFilesFolder;
	
	public CSVDataImportJob() {
		super();
	}

	@Override
	protected void buildTasks() throws Throwable {
		String extension = FilenameUtils.getExtension(input.file.getName());
		if (Files.ZIP_FILE_EXTENSION.equalsIgnoreCase(extension)) {
			tempInputFilesFolder = Files.createTempDirectory();
			ZipFiles.extract(input.file, tempInputFilesFolder);
			List<String> fileNames = Files.listFileNamesInFolder(tempInputFilesFolder);
			List<String> processedFileNames = new ArrayList<String>(fileNames);
			
			Map<String, EntityDefinition> entityDefinitionsByFileName = 
					new CSVDataExportJob.EntryNameGenerator(OutputFormat.CSV).generateMultipleEntitesEntryMap(input.survey);
			for (Entry<String, EntityDefinition> entry : entityDefinitionsByFileName.entrySet()) {
				String fileName = entry.getKey();
				EntityDefinition entityDef = entry.getValue();
				File file = new File(tempInputFilesFolder, fileName);
				if (file.exists()) {
					CSVDataImportTask task = createTask(CSVDataImportTask.class);
					CSVDataImportSettings settings = input.settings.clone();
					settings.setInsertNewRecords(entityDef.isRoot());
					task.input = new CSVDataImportInput(file, input.survey, input.steps, entityDef.getId(), settings);
					addTask(task);
					processedFileNames.add(fileName);
				}
			}
			if (processedFileNames.size() < fileNames.size()) {
				throw new IllegalStateException("Invalid file names found: some of the included files cannot be imported");
			}
		} else {
			CSVDataImportTask task = createTask(CSVDataImportTask.class);
			task.input = input;
			addTask(task);
		}
	}
	
	public CSVDataImportInput getInput() {
		return input;
	}

	public void setInput(CSVDataImportInput input) {
		this.input = input;
	}

	public List<DataParsingError> getParsingErrors() {
		List<DataParsingError> result = new ArrayList<DataParsingError>();
		for (Worker worker : getTasks()) {
			CSVDataImportTask task = (CSVDataImportTask) worker;
			ReferenceDataImportStatus<ParsingError> dataImportStatus = task.getDataImportStatus();
			if (dataImportStatus != null) {
				List<ParsingError> errors = dataImportStatus.getErrors();
				for (ParsingError parsingError : errors) {
					DataParsingError dataParsingError = new DataParsingError(task.getInput().getFile().getName(), parsingError.getRow(), 
							parsingError.getErrorType(), parsingError.getColumns(), parsingError.getMessage(), parsingError.getMessageArgs());
					result.add(dataParsingError);
				}
			}
		}
		return result;
	}
	
	@Override
	protected void onEnd() {
		super.onEnd();
		FileUtils.deleteQuietly(tempInputFilesFolder);
	}
	
	public static class DataParsingError extends ParsingError {

		private String fileName;
		
		public DataParsingError(String fileName, long row, ErrorType type, String[] columns, String message, String[] messageArgs) {
			super(type, row, columns, message);
			super.setMessageArgs(messageArgs);
			this.fileName = fileName;
		}
		
		public String getFileName() {
			return fileName;
		}
	}
	
	static class ImportException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public ImportException() {
			super();
		}
		
	}
	
	private static class RecordStepKey {
		private int id;
		private Step step;
		
		public RecordStepKey(int id, Step step) {
			super();
			this.id = id;
			this.step = step;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			result = prime * result + ((step == null) ? 0 : step.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RecordStepKey other = (RecordStepKey) obj;
			if (id != other.id)
				return false;
			if (step != other.step)
				return false;
			return true;
		}
	}
	
	public static class CSVDataImportInput {
		/**
		 * Input file
		 */
		private File file;
		/**
		 * Current survey
		 */
		private CollectSurvey survey;
		/**
		 * Record step that will be considered for insert or update
		 */
		private Set<Step> steps = new HashSet<Step>();
		/**
		 * Entity definition that should be considered as the parent of each attribute in the csv file
		 */
		private Integer parentEntityDefinitionId;
		
		private EntityDefinition parentEntityDefinition;
		
		private CSVDataImportSettings settings;

		public CSVDataImportInput(File file, CollectSurvey survey, Step[] steps, Integer parentEntityDefinitionId,
				CSVDataImportSettings settings) {
			this(file, survey, toSet(steps), parentEntityDefinitionId, settings);
		}
		
		public CSVDataImportInput(File file, CollectSurvey survey, Set<Step> steps, Integer parentEntityDefinitionId,
				CSVDataImportSettings settings) {
			super();
			this.file = file;
			this.survey = survey;
			this.steps = steps;
			this.settings = settings == null ? new CSVDataImportSettings(): settings;
			this.parentEntityDefinitionId = parentEntityDefinitionId;
			this.parentEntityDefinition = parentEntityDefinitionId == null ? null : (EntityDefinition) survey.getSchema().getDefinitionById(parentEntityDefinitionId);
		}

		private static Set<Step> toSet(Step[] arr) {
			if (arr == null) {
				return Collections.emptySet();
			} else {
				return new HashSet<Step>(Arrays.asList(arr));
			}
		}

		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
		}

		public CollectSurvey getSurvey() {
			return survey;
		}

		public void setSurvey(CollectSurvey survey) {
			this.survey = survey;
		}

		public Set<Step> getSteps() {
			return steps;
		}

		public void setSteps(Set<Step> steps) {
			this.steps = steps;
		}

		public Integer getParentEntityDefinitionId() {
			return parentEntityDefinitionId;
		}

		public void setParentEntityDefinitionId(Integer parentEntityDefinitionId) {
			this.parentEntityDefinitionId = parentEntityDefinitionId;
		}
		
		public EntityDefinition getParentEntityDefinition() {
			return parentEntityDefinition;
		}
		
		public CSVDataImportSettings getSettings() {
			return settings;
		}

		public void setSettings(CSVDataImportSettings settings) {
			this.settings = settings;
		}
		
	}
	
	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class CSVDataImportTask extends Task {

		private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "dataManagement.csvDataImport.error.internalErrorImportingFile";
		private static final String NO_RECORD_FOUND_ERROR_MESSAGE_KEY = "dataManagement.csvDataImport.error.noRecordFound";
		private static final String MULTIPLE_RECORDS_FOUND_ERROR_MESSAGE_KEY = "dataManagement.csvDataImport.error.multipleRecordsFound";
//		private static final String ONLY_NEW_RECORDS_ALLOWED_MESSAGE_KEY = "dataManagement.csvDataImport.error.onlyNewRecordsAllowed";
		private static final String MULTIPLE_PARENT_ENTITY_FOUND_MESSAGE_KEY = "dataManagement.csvDataImport.error.multipleParentEntityFound";
		private static final String PARENT_ENTITY_NOT_FOUND_MESSAGE_KEY = "dataManagement.csvDataImport.error.noParentEntityFound";
		private static final String UNIT_NOT_FOUND_MESSAGE_KEY = "dataManagement.csvDataImport.error.unitNotFound";
		private static final String SRS_NOT_FOUND_MESSAGE_KEY = "dataManagement.csvDataImport.error.srsNotFound";
		private static final String RECORD_NOT_IN_SELECTED_STEP_MESSAGE_KEY= "dataManagement.csvDataImport.error.recordNotInSelectedStep";
		private static final String NO_ROOT_ENTITY_SELECTED_ERROR_MESSAGE_KEY = "dataManagement.csvDataImport.error.noRootEntitySelected";
		private static final String NO_MODEL_VERSION_FOUND_ERROR_MESSAGE_KEY = "dataManagement.csvDataImport.error.noModelVersionFound";

		@Autowired
		private UserManager userManager;
		@Autowired
		private RecordManager recordManager;
		@Autowired(required=false)
		private NodeChangeBatchProcessor nodeChangeBatchProcessor;

		private CSVDataImportInput input;

		//transient variables
		private RecordUpdater recordUpdater;
		private CollectRecordSummary lastModifiedRecordSummary;
		private CollectRecord lastModifiedRecord;
		private User adminUser;
		private Set<RecordStepKey> deletedEntitiesRecordKeys;
		private transient DataCSVReader reader;
		private ReferenceDataImportStatus<ParsingError> dataImportStatus;

		public CSVDataImportTask() {
			deletedEntitiesRecordKeys = new HashSet<RecordStepKey>();
		}
		
		@Override
		protected void validateInput() throws Throwable {
			super.validateInput();
			
			if ( ! input.file.exists() || ! input.file.canRead() ) {
				setErrorMessage(IMPORTING_FILE_ERROR_MESSAGE_KEY);
				changeStatus(Status.FAILED);
			} else if ( input.settings.isInsertNewRecords() && input.parentEntityDefinition == null ) {
				setErrorMessage(NO_ROOT_ENTITY_SELECTED_ERROR_MESSAGE_KEY);
				changeStatus(Status.FAILED);
			} else if ( input.settings.isInsertNewRecords() && input.settings.getNewRecordVersionName() != null && 
					input.survey.getVersion(input.settings.getNewRecordVersionName()) == null) {
				setErrorMessage(NO_MODEL_VERSION_FOUND_ERROR_MESSAGE_KEY);
				setErrorMessageArgs(new String[]{input.settings.getNewRecordVersionName()});
				changeStatus(Status.FAILED);
			}
		}
		
		@Override
		protected void initializeInternalVariables() throws Throwable {
			super.initializeInternalVariables();
			recordUpdater = new RecordUpdater();
			recordUpdater.setValidateAfterUpdate(input.settings.isRecordValidationEnabled());
			dataImportStatus = new ReferenceDataImportStatus<ParsingError>();
			adminUser = userManager.loadAdminUser();
			EntityDefinition parentEntityDefn = input.parentEntityDefinition;
			reader = new DataCSVReader(input.file, parentEntityDefn);
			try {
				reader.init();
			} catch (ParsingException e) {
				dataImportStatus.addParsingError(1, e.getError());
				changeStatus(Status.FAILED);
			}
		}
		
		@Override
		protected long countTotalItems() {
			try {
				return reader.size();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		protected void execute() throws Throwable {
			incrementProcessedItems();
			
			long currentRowNumber = 1;
			while ( isRunning() ) {
				currentRowNumber ++;
				try {
					DataLine line = reader.readNextLine();
					if ( line != null ) {
						processLine(line);
						incrementProcessedItems();
					}
					if ( ! reader.isReady() ) {
						//end of file reached
						if ( input.steps.size() == 1 && lastModifiedRecordSummary != null ) {
							saveLastModifiedRecord();
						}
						break;
					}
				} catch (ParsingException e) {
					dataImportStatus.addParsingError(currentRowNumber, e.getError());
				}
			}
			if ( dataImportStatus.hasErrors() ) {
				changeStatus(Status.FAILED);
			}
		}

		@Override
		protected void onEnd() {
			super.onEnd();
			IOUtils.closeQuietly(reader);
		}
		
		private void processLine(DataLine line) throws RecordPersistenceException {
			if (! validateRecordKey(line) ) {
				return;
			}
			CollectRecordSummary recordSummary = loadRecordSummary(line);
			if (recordSummary == null && input.settings.isInsertNewRecords() ) {
				//create new record
				EntityDefinition rootEntityDefn = input.parentEntityDefinition;
				CollectRecord record = recordManager.instantiateRecord(input.survey, rootEntityDefn.getName(), 
						adminUser, input.settings.getNewRecordVersionName(), Step.ENTRY);
				NodeChangeSet changes = recordManager.initializeRecord(record);
				if (nodeChangeBatchProcessor != null) {
					nodeChangeBatchProcessor.add(changes, adminUser.getUsername());
				}
				setRecordKeys(line, record);
				setValuesInRecord(line, record, Step.ENTRY);
				insertRecord(record);
			} else if ( input.steps.size() > 1) {
				Step originalRecordStep = recordSummary.getStep();
				//set values in each step data
				for (Step currentStep : input.steps) {
					if ( currentStep.beforeEqual(originalRecordStep) ) {
						CollectRecord record = loadRecord(recordSummary.getId(), currentStep);
						setValuesInRecord(line, record, currentStep);
						//always save record when updating multiple record steps in the same process
						updateRecordData(record, originalRecordStep, currentStep);
					}
				}
			} else {
				Step originalRecordStep = recordSummary.getStep();
				Step inputStep = input.steps.iterator().next();
				if ( inputStep.beforeEqual(originalRecordStep) ) {
					CollectRecord record;
					boolean recordChanged = lastModifiedRecordSummary == null || ! recordSummary.getId().equals(lastModifiedRecordSummary.getId() );
					if ( recordChanged ) {
						//record changed
						if ( lastModifiedRecordSummary != null ) {
							saveLastModifiedRecord();
						}
						record = loadRecord(recordSummary.getId(), inputStep);
					} else {
						record = lastModifiedRecord;
					}
					setValuesInRecord(line, record, inputStep);
					lastModifiedRecordSummary = recordSummary;
					lastModifiedRecord = record;
				} else {
					dataImportStatus.addParsingError(new ParsingError(ErrorType.INVALID_VALUE, line.getLineNumber(), (String) null, 
							RECORD_NOT_IN_SELECTED_STEP_MESSAGE_KEY));
				}
			}
			dataImportStatus.addProcessedRow(line.getLineNumber());
		}

		private CollectRecord loadRecord(Integer recordId, Step step) {
			CollectRecord record = recordManager.load(input.survey, recordId, step, input.settings.isRecordValidationEnabled());
			//delete existing entities
			RecordStepKey recordStepKey = new RecordStepKey(record.getId(), step);
			if (input.settings.isDeleteExistingEntities() && ! deletedEntitiesRecordKeys.contains(recordStepKey) && ! input.parentEntityDefinition.isRoot()) {
				deleteAllParentEntities(record);
				deletedEntitiesRecordKeys.add(recordStepKey);
			}
			return record;
		}

		private void deleteAllParentEntities(CollectRecord record) {
			String parentEntitiesPath = input.parentEntityDefinition.getPath();
			List<Entity> entitiesToBeDeleted = record.findNodesByPath(parentEntitiesPath);
			for (Entity entity : entitiesToBeDeleted) {
				NodeChangeSet changes = recordUpdater.deleteNode(entity);
				if (nodeChangeBatchProcessor != null) {
					nodeChangeBatchProcessor.add(changes, adminUser.getUsername());
				}
			}
		}

		private void setRecordKeys(DataLine line, CollectRecord record) {
			EntityDefinition rootEntityDefn = record.getRootEntity().getDefinition();
			Value[] recordKeyValues = line.getRecordKeyValues(rootEntityDefn);

			List<AttributeDefinition> keyAttributeDefinitions = rootEntityDefn.getKeyAttributeDefinitions();
			for ( int i = 0; i < keyAttributeDefinitions.size(); i ++ ) {
				AttributeDefinition keyDefn = keyAttributeDefinitions.get(i);
				Attribute<?, ?> keyAttr = record.findNodeByPath(keyDefn.getPath() ); //for record key attributes, absolute path must be equal to relative path
				String value = ((AbstractValue) recordKeyValues[i]).toInternalString();
				if (keyDefn.isSingleFieldKeyAttribute()) {
					setValueInField(keyAttr, keyDefn.getMainFieldName(), value, line.getLineNumber(), null);
				} else {
					setValueInAttribute(keyAttr, value, line.getLineNumber(), null);
				}
			}
		}

		private void saveLastModifiedRecord() throws RecordPersistenceException {
			Step originalStep = lastModifiedRecordSummary.getStep();
			Step inputStep = input.steps.iterator().next();

			updateRecordData(lastModifiedRecord, originalStep, inputStep);
			
			if ( inputStep.compareTo(originalStep) < 0 ) {
				//reset record step to the original one
				CollectRecord record = recordManager.load(input.survey, lastModifiedRecordSummary.getId(), 
						originalStep, input.settings.isRecordValidationEnabled());
				record.setStep(originalStep);
				
				updateRecordData(record, originalStep, originalStep);
			}
		}
		
		private boolean validateRecordKey(DataLine line) {
			long currentRowNumber = line.getLineNumber();
			EntityDefinition parentEntityDefn = input.parentEntityDefinition;
			EntityDefinition rootEntityDefn = parentEntityDefn.getRootEntity();
			Value[] recordKeyValues = line.getRecordKeyValues(rootEntityDefn);
			RecordFilter filter = new RecordFilter(input.survey);
			filter.setRootEntityId(rootEntityDefn.getId());
			String[] recordKeysStringValues = Values.toStringValues(recordKeyValues);
			filter.setKeyValues(recordKeysStringValues);
			int recordCount = recordManager.countRecords(filter);
			String[] recordKeyColumnNames = DataCSVReader.getKeyAttributeColumnNames(
					parentEntityDefn,
					rootEntityDefn.getKeyAttributeDefinitions());
			String errorMessageKey = null;
			if ( input.settings.isInsertNewRecords()) {
//				if ( recordCount > 0 ) {
//					errorMessageKey = ONLY_NEW_RECORDS_ALLOWED_MESSAGE_KEY;
//				}
			} else if ( recordCount == 0 ) {
				errorMessageKey = NO_RECORD_FOUND_ERROR_MESSAGE_KEY;
			} else if ( recordCount > 1 ) {
				errorMessageKey = MULTIPLE_RECORDS_FOUND_ERROR_MESSAGE_KEY;
			}
			if ( errorMessageKey == null ) {
				return true;
			} else {
				ParsingError parsingError = new ParsingError(ErrorType.INVALID_VALUE, 
						currentRowNumber, recordKeyColumnNames, errorMessageKey);
				parsingError.setMessageArgs(new String[]{StringUtils.join(recordKeysStringValues, ", ")});
				dataImportStatus.addParsingError(currentRowNumber, parsingError);
				return false;
			}
		}
		
		private CollectRecordSummary loadRecordSummary(DataLine line) {
			EntityDefinition parentEntityDefn = input.parentEntityDefinition;
			EntityDefinition rootEntityDefn = parentEntityDefn.getRootEntity();
			Value[] recordKeyValues = line.getRecordKeyValues(rootEntityDefn);
			RecordFilter filter = new RecordFilter(input.survey);
			filter.setRootEntityId(rootEntityDefn.getId());
			filter.setKeyValues(Values.toStringValues(recordKeyValues));
			List<CollectRecordSummary> recordSummaries = recordManager.loadSummaries(filter);
			CollectRecordSummary recordSummary = recordSummaries.isEmpty() ? null : recordSummaries.get(0);
			return recordSummary;
		}

		private boolean setValuesInRecord(DataLine line, CollectRecord record, Step step) {
			//LOG.info("Setting values in record: " + record.getId() + "[" + record.getRootEntityKeyValues() + "]" + " step: " + step);
			record.setStep(step);
			Entity parentEntity = getOrCreateParentEntity(record, line);
			if ( parentEntity == null ) {
				//TODO add parsing error?
				return false;
			} else {
				setValuesInAttributes(parentEntity, line.getFieldValues(), line.getColumnNamesByField(), line.getLineNumber());
				return true;
			}
		}

		private void setValuesInAttributes(Entity ancestorEntity, Map<FieldValueKey, String> fieldValues, 
				Map<FieldValueKey, String> colNameByField, long row) {
			Set<Entry<FieldValueKey,String>> entrySet = fieldValues.entrySet();
			//delete all multiple attributes
			for (Entry<FieldValueKey, String> entry : entrySet) {
				FieldValueKey fieldValueKey = entry.getKey();
				EntityDefinition ancestorDefn = ancestorEntity.getDefinition();
				Schema schema = ancestorDefn.getSchema();
				AttributeDefinition attrDefn = (AttributeDefinition) schema.getDefinitionById(fieldValueKey.getAttributeDefinitionId());
				Entity parentEntity = getOrCreateParentEntity(ancestorEntity, attrDefn);
				if (attrDefn.isMultiple()) {
					List<Node<?>> attributes = parentEntity.getChildren(attrDefn);
					int tot = attributes.size();
					for (int i = 0; i < tot; i++) {
						Node<?> node = attributes.get(0);
						NodeChangeSet changes = recordUpdater.deleteNode(node);
						if (nodeChangeBatchProcessor != null) {
							nodeChangeBatchProcessor.add(changes, adminUser.getUsername());
						}
					}
				}
			}
			//set values
			for (Entry<FieldValueKey, String> entry : entrySet) {
				FieldValueKey fieldValueKey = entry.getKey();
				String strValue = entry.getValue();
				EntityDefinition ancestorDefn = ancestorEntity.getDefinition();
				Schema schema = ancestorDefn.getSchema();
				AttributeDefinition attrDefn = (AttributeDefinition) schema.getDefinitionById(fieldValueKey.getAttributeDefinitionId());
				String fieldName = fieldValueKey.getFieldName();
				Entity parentEntity = getOrCreateParentEntity(ancestorEntity, attrDefn);
				String colName = colNameByField.get(fieldValueKey);
				int attrPos = fieldValueKey.getAttributePosition();
				setValueInField(parentEntity, attrDefn, attrPos - 1, fieldName,
						strValue, colName, row);
			}
		}

		private void setValueInField(Entity parentEntity,
				AttributeDefinition attrDefn, int index, String fieldName,
				String value, String colName, long row) {
			String attrName = attrDefn.getName();
			Attribute<?, ?> attr = (Attribute<?, ?>) parentEntity.getChild(attrDefn, index);
			boolean emptyValue = StringUtils.isEmpty(value);
			if ( attr == null && ! emptyValue) {
				attr = (Attribute<?, ?>) performNodeAdd(parentEntity, attrName);
			}
			if (attr != null) {
				try {
					setValueInField(attr, fieldName, value, row, colName);
				} catch ( Exception e) {
					dataImportStatus.addParsingError(new ParsingError(ErrorType.INVALID_VALUE, row, colName, value));
				}
			}
		}
		
		private <V extends Value> void setValueInAttribute(Attribute<?, V> keyAttr, String value, long row, String colName) {
			try {
				V val = keyAttr.getDefinition().createValue(value);
				NodeChangeSet changes = recordUpdater.updateAttribute(keyAttr, val);
				if (nodeChangeBatchProcessor != null) {
					nodeChangeBatchProcessor.add(changes, adminUser.getUsername());
				}
			} catch ( Exception e) {
				dataImportStatus.addParsingError(new ParsingError(ErrorType.INVALID_VALUE, row, colName));
			}
		}
		
		private void setValueInField(Attribute<?, ?> attr, String fieldName, String value, long row, String colName) {
			if ( attr instanceof NumberAttribute && 
					(fieldName.equals(NumberAttributeDefinition.UNIT_FIELD) ||
					fieldName.equals(NumberAttributeDefinition.UNIT_NAME_FIELD)) ) {
				setUnitField(attr, value, row, colName);
			} else if ( attr instanceof CoordinateAttribute && fieldName.equals(CoordinateAttributeDefinition.SRS_FIELD_NAME) ) {
				setSRSIdField(attr, value, row, colName);
			} else {
				@SuppressWarnings("unchecked")
				Field<Object> field = (Field<Object>) attr.getField(fieldName);
				Object fieldValue = field.parseValue(value);
				NodeChangeSet changes = recordUpdater.updateField(field, fieldValue);
				if (nodeChangeBatchProcessor != null) {
					nodeChangeBatchProcessor.add(changes, adminUser.getUsername());
				}
			}
		}

		private void setSRSIdField(Attribute<?, ?> attr, String value, long row,
				String colName) {
			boolean valid = true;
			if ( StringUtils.isNotBlank(value) ) {
				//check SRS id validity
				Survey survey = attr.getSurvey();
				SpatialReferenceSystem srs = survey.getSpatialReferenceSystem(value);
				if ( srs == null ) {
					ParsingError parsingError = new ParsingError(ErrorType.INVALID_VALUE, row, colName, SRS_NOT_FOUND_MESSAGE_KEY);
					parsingError.setMessageArgs(new String[]{value});
					dataImportStatus.addParsingError(parsingError);
					valid = false;
				}
			}
			if ( valid ) {
				Field<String> field = ((CoordinateAttribute) attr).getSrsIdField();
				NodeChangeSet changes = recordUpdater.updateField(field, value);
				if (nodeChangeBatchProcessor != null) {
					nodeChangeBatchProcessor.add(changes, adminUser.getUsername());
				}
			}
		}

		private void setUnitField(Attribute<?, ?> attr, String value, long row,
				String colName) {
			if ( StringUtils.isBlank(value) ) {
				((NumberAttribute<?, ?>) attr).setUnit(null);
			} else {
				Survey survey = attr.getSurvey();
				Unit unit = survey.getUnit(value);
				NumericAttributeDefinition defn = (NumericAttributeDefinition) attr.getDefinition();
				if ( unit == null || ! defn.getUnits().contains(unit) ) {
					ParsingError parsingError = new ParsingError(ErrorType.INVALID_VALUE, row, colName, UNIT_NOT_FOUND_MESSAGE_KEY);
					parsingError.setMessageArgs(new String[]{value});
					dataImportStatus.addParsingError(parsingError);
				} else {
					Field<Integer> field = ((NumberAttribute<?, ?>) attr).getUnitField();
					NodeChangeSet changes = recordUpdater.updateField(field, unit.getId());
					if (nodeChangeBatchProcessor != null) {
						nodeChangeBatchProcessor.add(changes, adminUser.getUsername());
					}
				}
			}
		}

		private Entity getOrCreateParentEntity(Entity ancestorEntity, AttributeDefinition attrDefn) {
			EntityDefinition ancestorEntityDefn = ancestorEntity.getDefinition();
			List<EntityDefinition> attributeAncestors = attrDefn.getAncestorEntityDefinitionsInReverseOrder();
			int indexOfAncestorEntity = attributeAncestors.indexOf(ancestorEntityDefn);
			if ( indexOfAncestorEntity < 0 ) {
				throw new IllegalArgumentException("AttributeDefinition is not among the ancestor entity descendants");
			} else if ( indexOfAncestorEntity == attributeAncestors.size() - 1 ) {
				return ancestorEntity;
			} else {
				Entity currentParent = ancestorEntity;
				List<EntityDefinition> nearestAncestors = attributeAncestors.subList(indexOfAncestorEntity + 1, attributeAncestors.size());
				for (EntityDefinition ancestor : nearestAncestors) {
					if ( currentParent.getCount(ancestor) == 0 ) {
						Entity newNode = (Entity) ancestor.createNode();
						currentParent.add(newNode);
						currentParent = newNode;
					} else {
						currentParent = (Entity) currentParent.getChild(ancestor);
					}
				}
				return currentParent;
			}
		}

		private void updateRecordData(final CollectRecord record, Step originalRecordStep, Step dataStep) throws RecordPersistenceException {
			recordManager.updateRecordStepDataAndRun(record, dataStep, record.getDataWorkflowSequenceNumber(), adminUser, false, new Runnable() {
				public void run() {
					if (nodeChangeBatchProcessor != null) {
						nodeChangeBatchProcessor.process(record);
					}
				}
			});
		}

		private void insertRecord(final CollectRecord record) throws RecordPersistenceException {
			performRecordSave(record);
		}
		
		private void performRecordSave(final CollectRecord record) {
			recordManager.saveAndRun(record, new Runnable() {
				public void run() {
					if (nodeChangeBatchProcessor != null) {
						nodeChangeBatchProcessor.process(record);
					}
				}
			});
		}
		
		private Entity getOrCreateParentEntity(CollectRecord record, DataLine line) {
			EntityDefinition parentEntityDefn = input.parentEntityDefinition;
			Entity rootEntity = record.getRootEntity();
			Entity currentParent = rootEntity;
			List<EntityDefinition> ancestorEntityDefns = parentEntityDefn.getAncestorEntityDefinitionsInReverseOrder();
			ancestorEntityDefns.add(parentEntityDefn);
			//skip the root entity
			for (int i = 1; i < ancestorEntityDefns.size(); i++) {
				EntityDefinition ancestorDefn = ancestorEntityDefns.get(i);
				String ancestorName = ancestorDefn.getName();
				EntityIdentifier<?> identifier = line.getAncestorIdentifier(ancestorDefn.getId());
				Entity childEntity;
				if ( ancestorDefn.isMultiple() ) {
					List<Entity> childEntities = findChildEntities(currentParent, ancestorName, identifier);
					switch ( childEntities.size() ) {
					case 0:
						if ( input.settings.isCreateAncestorEntities() || ancestorDefn == parentEntityDefn ) {
							childEntity = createChildEntity(currentParent, ancestorName, identifier, line.getColumnNamesByField(), line.getLineNumber());
						} else {
							dataImportStatus.addParsingError(createParentEntitySearchError(record, line, identifier, PARENT_ENTITY_NOT_FOUND_MESSAGE_KEY));
							return null;
						}
						break;
					case 1:
						childEntity = childEntities.get(0);
						break;
					default:
						dataImportStatus.addParsingError(createParentEntitySearchError(record,
								line, identifier, MULTIPLE_PARENT_ENTITY_FOUND_MESSAGE_KEY));
						return null;
					}
				} else {
					if ( currentParent.getCount(ancestorDefn) == 0 ) {
						Node<?> newNode = ancestorDefn.createNode();
						currentParent.add(newNode);
					}
					childEntity = (Entity) currentParent.getChild(ancestorDefn);
				}
				currentParent = childEntity;
			}
			return currentParent;
		}

		private List<Entity> findChildEntities(Entity currentParent, String childName, EntityIdentifier<?> identifier) {
			if ( identifier instanceof EntityPositionIdentifier ) {
				int position = ((EntityPositionIdentifier) identifier).getPosition();
				if ( currentParent.getCount(childName) >= position ) {
					ArrayList<Entity> result = new ArrayList<Entity>();
					Entity child = (Entity) currentParent.getChild(childName, position - 1);
					result.add(child);
					return result;
				} else {
					return Collections.emptyList();
				}
			} else {
				EntityDefinition parentDefn = currentParent.getDefinition();
				EntityDefinition childDefn = parentDefn.getChildDefinition(childName, EntityDefinition.class);
				Value[] keyValues = ((EntityKeysIdentifier) identifier).getKeyValues();
				return currentParent.findChildEntitiesByKeys(childDefn, keyValues);
			}
		}
		
		private Entity createChildEntity(Entity currentParent, String childName,
				EntityIdentifier<?> identifier,
				Map<FieldValueKey, String> colNamesByField, long row) {
			if ( identifier instanceof EntityPositionIdentifier ) {
				int position = ((EntityPositionIdentifier) identifier).getPosition();
				if ( position == currentParent.getCount(childName) + 1 ) {
					Entity entity = (Entity) performNodeAdd(currentParent, childName);
					return entity;
				} else {
					throw new IllegalArgumentException(
							String.format("Trying to create child in a invalid position: row=%d path=%s[%d]",
									row, 
									currentParent.getPath() + "/" + childName,
									position));
				}
			} else {
				Entity entity = (Entity) performNodeAdd(currentParent, childName);
				Value[] keyValues = ((EntityKeysIdentifier) identifier).getKeyValues();
				setKeyValues(entity, keyValues, colNamesByField, row);
				return entity;
			}
		}

		private Node<?> performNodeAdd(Entity parent, String childName) {
			NodeChangeSet changeSet = recordUpdater.addNode(parent, childName);
			for (NodeChange<?> nodeChange : changeSet.getChanges()) {
				if ( nodeChange instanceof NodeAddChange ) {
					return nodeChange.getNode();
				}
			}
			throw new RuntimeException(String.format("Error adding new entity with name %s to parent %s", childName, parent.getPath()));
		}

		private ParsingError createParentEntitySearchError(CollectRecord record, DataLine line, 
				EntityIdentifier<?> identifier, String messageKey) {
			EntityIdentifierDefinition identifierDefn = identifier.getDefinition();
			Survey survey = record.getSurvey();
			Schema schema = survey.getSchema();
			EntityDefinition parentEntityDefn = (EntityDefinition) schema.getDefinitionById(identifierDefn.getEntityDefinitionId());

			String[] colNames = DataCSVReader.getKeyAttributeColumnNames(parentEntityDefn, parentEntityDefn.getKeyAttributeDefinitions());
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, line.getLineNumber(), colNames, messageKey);
			List<String> recordKeys = new ArrayList<String>(record.getRootEntityKeyValues());
			CollectionUtils.filter(recordKeys, new Predicate<String>() {
				public boolean evaluate(String key) {
					return StringUtils.isNotBlank(key);
				}
			});
			String jointRecordKeys = StringUtils.join(recordKeys, ", ");
			String jointParentEntityKeys = identifier instanceof EntityPositionIdentifier ? 
					"[" + ((EntityPositionIdentifier) identifier).getPosition() + "]" :
					StringUtils.join(((EntityKeysIdentifier) identifier).getKeyValues(), ", ");
			error.setMessageArgs(new String[]{parentEntityDefn.getName(), jointParentEntityKeys, jointRecordKeys});
			return error;
		}
		
		private void setKeyValues(Entity entity, Value[] values, Map<FieldValueKey, String> colNameByField, long row) {
			//create key attribute values by name
			Map<FieldValueKey, String> keyValuesByField = new HashMap<FieldValueKey, String>();
			EntityDefinition entityDefn = entity.getDefinition();
			List<AttributeDefinition> keyDefns = entityDefn.getKeyAttributeDefinitions();
			for (int i = 0; i < keyDefns.size(); i++) {
				AttributeDefinition keyDefn = keyDefns.get(i);
				Value keyValue = values[i];
				Map<String, Object> keyValueMap = keyValue.toMap();
				List<String> keyFieldNames = keyDefn.getKeyFieldNames();
				for (String keyFieldName : keyFieldNames) {
					Object keyValueFieldVal = keyValueMap.get(keyFieldName);
					keyValuesByField.put(new FieldValueKey(keyDefn, keyFieldName), keyValueFieldVal.toString());
				}
			}
			setValuesInAttributes(entity, keyValuesByField, colNameByField, row);
		}
		
		public CSVDataImportInput getInput() {
			return input;
		}
		
		public void setInput(CSVDataImportInput input) {
			this.input = input;
		}
		
		public ReferenceDataImportStatus<ParsingError> getDataImportStatus() {
			return dataImportStatus;
		}
	}
	
}
