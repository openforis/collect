/**
 * 
 */
package org.openforis.collect.manager.dataimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.referencedataimport.ParsingException;
import org.openforis.collect.manager.referencedataimport.ReferenceDataImportStatus;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.utils.OpenForisIOUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.TextAttribute;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;

/**
 * @author S. Ricci
 *
 */
public class CSVDataImportProcess extends AbstractProcess<Void, ReferenceDataImportStatus<ParsingError>> {

	private static Log LOG = LogFactory.getLog(CSVDataImportProcess.class);

	private static final String CSV = "csv";
	private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "dataImport.error.internalErrorImportingFile";
	private static final String NO_RECORD_FOUND_ERROR_MESSAGE_KEY = "dataImport.error.noRecordFound";
	private static final String MULTIPLE_RECORDS_FOUND_ERROR_MESSAGE_KEY = "dataImport.error.multipleRecordsFound";
	private static final String NO_PARENT_ENTITY_FOUND = "dataImport.error.parentEntityNotFound";
	
	private RecordDao recordDao;
	private File file;
	private CollectSurvey survey;
	private int parentEntityDefinitionId;
	private boolean overwriteData;
	private DataCSVReader reader;
	
	public CSVDataImportProcess(RecordDao recordDao,
			File file, CollectSurvey survey, int parentEntityDefinitionId, boolean overwriteData) {
		this.recordDao = recordDao;
		this.file = file;
		this.survey = survey;
		this.parentEntityDefinitionId = parentEntityDefinitionId;
		this.overwriteData = overwriteData;
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
	
	protected void validateParameters() {
		if ( ! file.exists() && ! file.canRead() ) {
			status.error();
			status.setErrorMessage(IMPORTING_FILE_ERROR_MESSAGE_KEY);
		}
	}
	
	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		processFile();
	}

	protected void processFile() throws IOException {
		String fileName = file.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if ( CSV.equalsIgnoreCase(extension) ) {
			parseCSVLines(file);
		} else {
			String errorMessage = "File type not supported" + extension;
			status.setErrorMessage(errorMessage);
			status.error();
			LOG.error("Species import: " + errorMessage);
		}
		if ( status.hasErrors() ) {
			status.error();
		} else if ( status.isRunning() ) {
			status.complete();
		}
	}

	protected void parseCSVLines(File file) {
		InputStreamReader isReader = null;
		FileInputStream is = null;
		long currentRowNumber = 0;
		reader = null;
		try {
			is = new FileInputStream(file);
			isReader = OpenForisIOUtils.toReader(is);
			EntityDefinition parentEntityDefn = (EntityDefinition) survey.getSchema().getDefinitionById(parentEntityDefinitionId);
			reader = new DataCSVReader(isReader, parentEntityDefn);
			status.addProcessedRow(1);
			currentRowNumber = 2;
			while ( status.isRunning() ) {
				try {
					DataLine line = reader.readNextLine();
					if ( line != null ) {
						EntityDefinition rootEntityDefn = parentEntityDefn.getRootEntity();
						String[] recordKeyValues = line.getRecordKeyValues(rootEntityDefn);
						List<CollectRecord> recordSummaries = recordDao.loadSummaries(survey, rootEntityDefn.getName(), recordKeyValues);
						if ( recordSummaries.size() == 0 ) {
							status.addParsingError(currentRowNumber, new ParsingError(ErrorType.INVALID_VALUE, 
									currentRowNumber, (String) null, NO_RECORD_FOUND_ERROR_MESSAGE_KEY));
						} else if ( recordSummaries.size() > 1 ) {
							status.addParsingError(currentRowNumber, new ParsingError(ErrorType.INVALID_VALUE, 
									currentRowNumber, (String) null, MULTIPLE_RECORDS_FOUND_ERROR_MESSAGE_KEY));
						} else {
							CollectRecord recordSummary = recordSummaries.get(0);
							CollectRecord record = recordDao.load(survey, recordSummary.getId(), recordSummary.getStep().getStepNumber());
							Entity parentEntity = getParentEntity(record, currentRowNumber, line.getAncestorKeys());
							if ( parentEntity != null ) {
								setValuesInAttributes(parentEntity, line.getAttributeValues(), currentRowNumber);
								recordDao.update(record);
								status.addProcessedRow(currentRowNumber);
							}
						}
					}
					if ( ! reader.isReady() ) {
						break;
					}
				} catch (ParsingException e) {
					status.addParsingError(currentRowNumber, e.getError());
				} finally {
					currentRowNumber ++;
				}
			}
			status.setTotal(reader.getLinesRead() + 1);
		} catch (ParsingException e) {
			status.error();
			status.addParsingError(1, e.getError());
		} catch (Exception e) {
			status.error();
			status.addParsingError(currentRowNumber, new ParsingError(ErrorType.IOERROR, e.toString()));
			LOG.error("Error importing species CSV file", e);
		} finally {
			try {
				if ( reader != null ) {
					reader.close();
				}
			} catch (IOException e) {
				LOG.error("Error closing reader", e);
			}
		}
	}

	private void setValuesInAttributes(Entity parentEntity,
			Map<String, String> attributeValuesByName, long row) {
		Set<String> attributeNames = attributeValuesByName.keySet();
		for (String name : attributeNames) {
			Attribute<?, ?> attr = (Attribute<?, ?>) parentEntity.getChild(name);
			String strValue = attributeValuesByName.get(name);
			if ( attr == null ) {
				EntityDefinition parentDefn = parentEntity.getDefinition();
				AttributeDefinition attrDefn = (AttributeDefinition) parentDefn.getChildDefinition(name);
				attr = (Attribute<?, ?>) attrDefn.createNode();
				parentEntity.add(attr);
			}
			try {
				setValueInAttribute(attr, strValue);
			} catch ( Exception e) {
				status.addParsingError(new ParsingError(ErrorType.INVALID_VALUE, row, attr.getName(), strValue));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Value> void setValueInAttribute(Attribute<?, T> attr, String strVal) {
		T val;
		if ( attr instanceof DateAttribute ) {
			val = (T) Date.parseDate(strVal);
		} else if ( attr instanceof CodeAttribute ) {
			val = (T) new Code(strVal);
		} else if ( attr instanceof CoordinateAttribute ) {
			val = (T) Coordinate.parseCoordinate(strVal);
//		} else if ( attr instanceof NumberAttribute ) {
//			Type type = ((NumberAttributeDefinition) attr.getDefinition()).getType();
//			Field<?> numberField = ((NumberAttribute<?, ?>) attr).getNumberField();
//			Number value = (Number) numberField.parseValue(strVal);
//			//TODO set unit
//			Unit unit = null;
//			switch ( type ) {
//			case INTEGER: 
//				val = (T) new IntegerValue((Integer) value, unit);
//				break;
//			case REAL:
//				val = (T) new RealValue((Double) value, unit);
//			}
		} else if ( attr instanceof TextAttribute ) {
			val = (T) new TextValue(strVal);
		} else {
			throw new UnsupportedOperationException("Attribute type not supported: " + attr.getClass().getName());
		}
		attr.setValue(val);
	}

	private Entity getParentEntity(CollectRecord record, long row, Map<AttributeDefinition, String> ancestorKeyValues) {
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
			String[] ancestorKeys = getAncestorKeyValues(ancestorKeyValues, ancestorDefn.getKeyAttributeDefinitions());
			Entity childEntity = currentParent.getChildEntityByKeys(ancestorDefn.getName(), ancestorKeys);
			if ( childEntity == null ) {
				status.addParsingError(new ParsingError(ErrorType.INVALID_VALUE, row, (String) null, NO_PARENT_ENTITY_FOUND));
				return null;
			} else {
				currentParent = childEntity;
			}
		}
		return currentParent;
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
	
}
