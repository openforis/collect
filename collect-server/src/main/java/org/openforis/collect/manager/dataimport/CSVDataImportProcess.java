/**
 * 
 */
package org.openforis.collect.manager.dataimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.referencedataimport.ParsingException;
import org.openforis.collect.manager.referencedataimport.ReferenceDataImportStatus;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.utils.OpenForisIOUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.NumberAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class CSVDataImportProcess extends AbstractProcess<Void, ReferenceDataImportStatus<ParsingError>> {

	private static Log LOG = LogFactory.getLog(CSVDataImportProcess.class);

	private static final String CSV = "csv";
	private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "csvDataImport.error.internalErrorImportingFile";
	private static final String NO_RECORD_FOUND_ERROR_MESSAGE_KEY = "csvDataImport.error.noRecordFound";
	private static final String MULTIPLE_RECORDS_FOUND_ERROR_MESSAGE_KEY = "csvDataImport.error.multipleRecordsFound";
	private static final String NO_PARENT_ENTITY_FOUND = "csvDataImport.error.parentEntityNotFound";
	private static final String UNIT_NOT_FOUND = "csvDataImport.error.unitNotFound";
	private static final String SRS_NOT_FOUND = "csvDataImport.error.srsNotFound";

	@Autowired
	private RecordDao recordDao;
	@Autowired
	private RecordManager recordManager;
	
	private File file;
	private CollectSurvey survey;
	private Step step;
	private int parentEntityDefinitionId;
	
	public CSVDataImportProcess() {
	}
	
	@Override
	public void init() {
		super.init();
		validateParameters();
	}
	
	@Override
	protected void initStatus() {
		status = new ReferenceDataImportStatus<ParsingError>();
	}
	
	@Override
	@Transactional(rollbackFor=ImportException.class)
	public Void call() throws Exception {
		Void result = super.call();
		if ( ! status.isComplete() ) {
			//rollback transaction
			throw new ImportException();
		}
		return result;
	}
	
	protected void validateParameters() {
		if ( ! file.exists() || ! file.canRead() ) {
			status.error();
			status.setErrorMessage(IMPORTING_FILE_ERROR_MESSAGE_KEY);
		} else {
			String fileName = file.getName();
			String extension = FilenameUtils.getExtension(fileName);
			if ( !  CSV.equalsIgnoreCase(extension) ) {
				String errorMessage = "File type not supported" + extension;
				status.setErrorMessage(errorMessage);
				status.error();
				LOG.error("Error importing file: " + errorMessage);
			}
		}
	}
	
	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		processFile();
	}

	protected void processFile() throws ImportException {
		InputStreamReader isReader = null;
		FileInputStream is = null;
		long currentRowNumber = 0;
		DataCSVReader reader = null;
		try {
			is = new FileInputStream(file);
			isReader = OpenForisIOUtils.toReader(is);
			EntityDefinition parentEntityDefn = getParentEntityDefinition();
			reader = new DataCSVReader(isReader, parentEntityDefn);
			status.addProcessedRow(1);
			currentRowNumber = 1;
			while ( status.isRunning() ) {
				currentRowNumber ++;
				try {
					DataLine line = reader.readNextLine();
					if ( line != null ) {
						processLine(line);
					}
					if ( ! reader.isReady() ) {
						break;
					}
				} catch (ParsingException e) {
					status.addParsingError(currentRowNumber, e.getError());
				}
			}
			status.setTotal(reader.getLinesRead() + 1);
			if ( status.hasErrors() ) {
				status.error();
			} else if ( status.isRunning() ) {
				status.complete();
			}
		} catch (ParsingException e) {
			status.error();
			status.addParsingError(1, e.getError());
		} catch (Exception e) {
			status.error();
			status.addParsingError(currentRowNumber, new ParsingError(ErrorType.IOERROR, e.toString()));
			LOG.error("Error importing CSV file", e);
		} finally {
			close(reader);
		}
	}

	private void processLine(DataLine line) {
		long currentRowNumber = line.getLineNumber();
		EntityDefinition parentEntityDefn = getParentEntityDefinition();
		EntityDefinition rootEntityDefn = parentEntityDefn.getRootEntity();
		String[] recordKeyValues = line.getRecordKeyValues(rootEntityDefn);
		List<CollectRecord> recordSummaries = recordDao.loadSummaries(survey, rootEntityDefn.getName(), recordKeyValues);
		String[] recordKeyColumnNames = DataCSVReader.getKeyAttributeColumnNames(
				parentEntityDefn,
				rootEntityDefn.getKeyAttributeDefinitions());
		if ( recordSummaries.size() == 0 ) {
			ParsingError parsingError = new ParsingError(ErrorType.INVALID_VALUE, 
					currentRowNumber, recordKeyColumnNames, NO_RECORD_FOUND_ERROR_MESSAGE_KEY);
			parsingError.setMessageArgs(new String[]{StringUtils.join(recordKeyValues)});
			status.addParsingError(currentRowNumber, parsingError);
		} else if ( recordSummaries.size() > 1 ) {
			ParsingError parsingError = new ParsingError(ErrorType.INVALID_VALUE, 
					currentRowNumber, recordKeyColumnNames, MULTIPLE_RECORDS_FOUND_ERROR_MESSAGE_KEY);
			parsingError.setMessageArgs(new String[]{StringUtils.join(recordKeyValues)});
			status.addParsingError(currentRowNumber, parsingError);
		} else {
			CollectRecord recordSummary = recordSummaries.get(0);
			setValuesInRecord(line, recordSummary);
			status.addProcessedRow(currentRowNumber);
		}
	}

	private void setValuesInRecord(DataLine line, CollectRecord recordSummary) {
		if ( step == null ) {
			for (Step currentStep : Step.values()) {
				Step recordStep = recordSummary.getStep();
				if ( currentStep.compareTo(recordStep) <= 0  ) {
					setValuesInRecord(line, recordSummary,
							currentStep);
				}
			}
		}
	}

	private void setValuesInRecord(DataLine line, CollectRecord recordSummary, Step step) {
		LOG.info("Setting values in record: " + recordSummary.getId() + "[" + recordSummary.getRootEntityKeyValues() + "]" + " step: " + step);
		
		CollectRecord record = recordDao.load(survey, recordSummary.getId(), step.getStepNumber());
		Entity parentEntity = getOrCreateParentEntity(record, line);
		if ( parentEntity != null ) {
			setValuesInAttributes(parentEntity, line);
			if ( step == Step.ANALYSIS ) {
				record.setStep(Step.CLEANSING);
				recordDao.update(record);
				record.setStep(Step.ANALYSIS);
			}
			recordManager.validate(record);
			recordDao.update(record);
		}
	}

	private void setValuesInAttributes(Entity parentEntity, DataLine line) {
		Map<FieldDefinition<?>, String> fieldValues = line.getFieldValues();
		long row = line.getLineNumber();
		Map<FieldDefinition<?>, String> colNamesByField = line.getColumnNamesByField();
		setValuesInAttributes(parentEntity, fieldValues, colNamesByField, row);
	}
	
	private void setValuesInAttributes(Entity parentEntity, Map<FieldDefinition<?>, String> fieldValues, 
			Map<FieldDefinition<?>, String> colNameByField, long row) {
		Set<FieldDefinition<?>> fieldDefns = fieldValues.keySet();
		for (FieldDefinition<?> fieldDefn : fieldDefns) {
			AttributeDefinition attrDefn = fieldDefn.getAttributeDefinition();
			String attrName = attrDefn.getName();
			Attribute<?, ?> attr = (Attribute<?, ?>) parentEntity.getChild(attrName);
			if ( attr == null ) {
				attr = (Attribute<?, ?>) attrDefn.createNode();
				parentEntity.add(attr);
			}
			String strValue = fieldValues.get(fieldDefn);
			String colName = colNameByField.get(fieldDefn);
			try {
				setValueInField(attr, fieldDefn.getName(), strValue, row, colName);
				//setValueInAttribute(attr, strValue);
			} catch ( Exception e) {
				status.addParsingError(new ParsingError(ErrorType.INVALID_VALUE, row, colName));
			}
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
			field.setValueFromString(value);
		}
	}

	private void setSRSIdField(Attribute<?, ?> attr, String value, long row,
			String colName) {
		Survey survey = attr.getSurvey();
		SpatialReferenceSystem srs = survey.getSpatialReferenceSystem(value);
		if ( srs == null ) {
			ParsingError parsingError = new ParsingError(ErrorType.INVALID_VALUE, row, colName, SRS_NOT_FOUND);
			parsingError.setMessageArgs(new String[]{value});
			status.addParsingError(parsingError);
		} else {
			((CoordinateAttribute) attr).getSrsIdField().setValue(value);
		}
	}

	private void setUnitField(Attribute<?, ?> attr, String value, long row,
			String colName) {
		Survey survey = attr.getSurvey();
		Unit unit = survey.getUnit(value);
		NumericAttributeDefinition defn = (NumericAttributeDefinition) attr.getDefinition();
		if ( unit == null || ! defn.getUnits().contains(unit) ) {
			ParsingError parsingError = new ParsingError(ErrorType.INVALID_VALUE, row, colName, UNIT_NOT_FOUND);
			parsingError.setMessageArgs(new String[]{value});
			status.addParsingError(parsingError);
		} else {
			((NumberAttribute<?, ?>) attr).setUnit(unit);
		}
	}

//	@SuppressWarnings("unchecked")
//	private <T extends Value> void setValueInAttribute(Attribute<?, T> attr, String strVal) {
//		T val;
//		if ( attr instanceof DateAttribute ) {
//			val = (T) Date.parseDate(strVal);
//		} else if ( attr instanceof CodeAttribute ) {
//			val = (T) new Code(strVal);
//		} else if ( attr instanceof CoordinateAttribute ) {
//			val = (T) Coordinate.parseCoordinate(strVal);
//		} else if ( attr instanceof TextAttribute ) {
//			val = (T) new TextValue(strVal);
//		} else {
//			throw new UnsupportedOperationException("Attribute type not supported: " + attr.getClass().getName());
//		}
//		attr.setValue(val);
//	}

	private Entity getOrCreateParentEntity(CollectRecord record, DataLine line) {
		Map<AttributeDefinition, String> ancestorKeyByDefinition = line.getAncestorKeys();
		long row = line.getLineNumber();
		Survey survey = record.getSurvey();
		Schema schema = survey.getSchema();
		EntityDefinition parentEntityDefn = (EntityDefinition) schema.getDefinitionById(parentEntityDefinitionId);
		Entity rootEntity = record.getRootEntity();
		Entity currentParent = rootEntity;
		List<EntityDefinition> ancestorEntityDefns = parentEntityDefn.getAncestorEntityDefinitions();
		ancestorEntityDefns.add(parentEntityDefn);
		//skip the root entity
		for (int i = 1; i < ancestorEntityDefns.size(); i++) {
			EntityDefinition ancestorDefn = ancestorEntityDefns.get(i);
			String[] ancestorKeys = getAncestorKeyValues(ancestorKeyByDefinition, ancestorDefn.getKeyAttributeDefinitions());
			Entity childEntity;
			if ( ancestorDefn.isMultiple() ) {
				childEntity = currentParent.getChildEntityByKeys(ancestorDefn.getName(), ancestorKeys);
				if ( childEntity == null ) {
					if ( i == ancestorEntityDefns.size() - 1 ) {
						//create entity
						childEntity = (Entity) ancestorDefn.createNode();
						currentParent.add(childEntity);
						setKeyValues(childEntity, ancestorKeys, line.getColumnNamesByField(), row);
					} else {
						status.addParsingError(new ParsingError(ErrorType.INVALID_VALUE, row, (String) null, NO_PARENT_ENTITY_FOUND));
						return null;
					}
				}
			} else {
				childEntity = (Entity) currentParent.getChild(ancestorDefn.getName());
			}
			currentParent = childEntity;
		}
		if ( currentParent == null ) {
			status.addParsingError(new ParsingError(ErrorType.INVALID_VALUE, row, (String) null, NO_PARENT_ENTITY_FOUND));
		}
		return currentParent;
	}
	

	private void setKeyValues(Entity entity, String[] values, Map<FieldDefinition<?>, String> colNamesByField, long row) {
		//create key attribute values by name
		Map<FieldDefinition<?>, String> keyValuesByField = new HashMap<FieldDefinition<?>, String>();
		EntityDefinition entityDefn = entity.getDefinition();
		List<AttributeDefinition> keyDefns = entityDefn.getKeyAttributeDefinitions();
		for (int i = 0; i < keyDefns.size(); i++) {
			AttributeDefinition keyDefn = keyDefns.get(i);
			String mainFieldName = keyDefn.getMainFieldName();
			FieldDefinition<?> mainField = keyDefn.getFieldDefinition(mainFieldName);
			keyValuesByField.put(mainField, values[i]);
		}
		setValuesInAttributes(entity, keyValuesByField, colNamesByField, row);
	}

	private String[] getAncestorKeyValues(Map<AttributeDefinition, String> ancestorKeyValuesByDefinition,
			List<AttributeDefinition> keyAttributeDefns) {
		String[] result = new String[keyAttributeDefns.size()];
		for (int i = 0; i < keyAttributeDefns.size(); i++) {
			AttributeDefinition keyDefn = keyAttributeDefns.get(i);
			String value = ancestorKeyValuesByDefinition.get(keyDefn);
			result[i] = value;
		}
		return result;
	}

	private void close(DataCSVReader reader) {
		try {
			if ( reader != null ) {
				reader.close();
			}
		} catch (IOException e) {
			LOG.error("Error closing reader", e);
		}
	}

	private EntityDefinition getParentEntityDefinition() {
		return (EntityDefinition) survey.getSchema().getDefinitionById(parentEntityDefinitionId);
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
	
	public Step getStep() {
		return step;
	}
	
	public void setStep(Step step) {
		this.step = step;
	}

	public int getParentEntityDefinitionId() {
		return parentEntityDefinitionId;
	}
	
	public void setParentEntityDefinitionId(int parentEntityDefinitionId) {
		this.parentEntityDefinitionId = parentEntityDefinitionId;
	}
	
	static class ImportException extends Exception {

		private static final long serialVersionUID = 1L;

		public ImportException() {
			super();
		}
		
	}
}
