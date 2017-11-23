/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.openforis.collect.io.data.DataLine.EntityIdentifier;
import org.openforis.collect.io.data.DataLine.EntityIdentifierDefinition;
import org.openforis.collect.io.data.DataLine.EntityKeysIdentifier;
import org.openforis.collect.io.data.DataLine.EntityKeysIdentifierDefintion;
import org.openforis.collect.io.data.DataLine.EntityPositionIdentifierDefinition;
import org.openforis.collect.io.data.DataLine.FieldValueKey;
import org.openforis.collect.io.data.DataLine.SingleEntityIdentifierDefinition;
import org.openforis.collect.io.data.csv.BasicColumnProvider;
import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.collect.io.data.csv.CodeColumnProvider;
import org.openforis.collect.io.data.csv.ColumnProvider;
import org.openforis.collect.io.data.csv.ColumnProviderChain;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.CSVDataImportReader;
import org.openforis.collect.io.metadata.parsing.CSVLineParser;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.AbstractValue;
import org.openforis.idm.model.Value;

/**
 * @author S. Ricci
 *
 */
public class DataCSVReader extends CSVDataImportReader<DataLine> {

	private static final Pattern MULTIPLE_ATTRIBUTE_COLUMN_NAME_PATTERN = Pattern.compile("^.*\\[(\\d+)\\]$");
	private static final String ATTRIBUTE_FIELD_SEPARATOR = "_";
	private static final String POSITION_COLUMN_FORMAT = "_%s_position";

	private static final String MISSING_REQUIRED_COLUMNS_MESSAGE_KEY = "dataManagement.csvDataImport.error.missing-required-columns";
	private static final String INVALID_NODE_POSITION_VALUE_MESSAGE_KEY = "dataManagement.csvDataImport.error.invalid-node-position";
	private static final String ROW_IDENTIFIER_NOT_SPECIFIED_MESSAGE_KEY = "dataManagement.csvDataImport.error.row-identifier-not-specified";

	//input variables
	private EntityDefinition parentEntityDefinition;
	
	//temporary variables
	private List<String> ignoredColumns = new ArrayList<String>();
	
	public DataCSVReader(File file, EntityDefinition parentEntityDefinition) throws IOException, ParsingException {
		super(file);
		this.parentEntityDefinition = parentEntityDefinition;
	}

	@Deprecated
	public DataCSVReader(Reader reader, EntityDefinition parentEntityDefinition) throws IOException, ParsingException {
		super(reader);
		this.parentEntityDefinition = parentEntityDefinition;
	}
	
	@Override
	protected DataCSVLineParser createLineParserInstance() {
		DataCSVLineParser lineParser = new DataCSVLineParser(this);
		return lineParser;
	}

	@Override
	public boolean validateAllFile() throws ParsingException {
		this.ignoredColumns = calculateIgnoredColumns();
		Validator validator = new Validator();
		validator.validate();
		return true;
	}
	
	private List<String> calculateIgnoredColumns() {
		final CSVDataExportParameters csvExportConfig = new CSVDataExportParameters();
		csvExportConfig.setIncludeCodeItemLabelColumn(true);
		csvExportConfig.setIncludeEnumeratedEntities(false);
		
		CollectSurvey survey = (CollectSurvey) parentEntityDefinition.getSurvey();
		
		ColumnProviderChain columnProvider = new CSVDataExportColumnProviderGenerator(survey, csvExportConfig)
				.generateColumnProviderChain(parentEntityDefinition);
		
		List<String> result = new ArrayList<String>();
		List<String> colNames = getColumnNames();
		for (final String colName : colNames) {
			final MutableBoolean ignored = new MutableBoolean(false);
			
			columnProvider.traverseProviders(new Visitor<ColumnProvider>() {
				public void visit(ColumnProvider p) {
					if (! (p instanceof ColumnProviderChain) && p instanceof BasicColumnProvider) {
						List<String> finalColumnHeadings = ((BasicColumnProvider) p).generateFinalColumnHeadings();
						if (finalColumnHeadings.contains(colName)) {
							if (p instanceof CodeColumnProvider &&
									colName.endsWith(csvExportConfig.getFieldHeadingSeparator() + CodeColumnProvider.ITEM_LABEL_SUFFIX)) {
								ignored.setTrue();
							}
						}
					}
				}
			});
			if (ignored.booleanValue()) {
				result.add(colName); 
			}
		}
		return result;
	}
	
	private boolean isIgnoredInDataExport(String columnName) {
		return ignoredColumns.contains(columnName);
	}

	protected static List<String> getKeyAttributeColumnNames(
			EntityDefinition parentEntityDefinition,
			AttributeDefinition keyAttrDefn) {
		EntityDefinition parentDefn = keyAttrDefn.getParentEntityDefinition();
		String prefix = parentDefn == parentEntityDefinition ? "" : parentDefn.getName() + "_";
		List<String> keyFieldNames = keyAttrDefn.getKeyFieldNames();
		if (keyFieldNames.size() == 1) {
			return Arrays.asList(prefix + keyAttrDefn.getName());
		} else {
			List<String> result = new ArrayList<String>(keyFieldNames.size());
			for (String fieldName : keyFieldNames) {
				result.add(prefix + keyAttrDefn.getName() + "_" + fieldName);
			}
			return result;
		}
	}
	
	protected static String getPositionColumnName(EntityDefinition defn) {
		return String.format(POSITION_COLUMN_FORMAT, defn.getName());
	}

	protected static String[] getKeyAttributeColumnNames(EntityDefinition parentEntityDefinition, 
			List<AttributeDefinition> ancestorKeyAttrDefns) {
		List<String> result = new ArrayList<String>(ancestorKeyAttrDefns.size());
		for (int i = 0; i < ancestorKeyAttrDefns.size(); i++) {
			AttributeDefinition attrDefn = ancestorKeyAttrDefns.get(i);
			result.addAll(getKeyAttributeColumnNames(parentEntityDefinition, attrDefn));
		}
		return result.toArray(new String[result.size()]);
	}
	
	protected List<AttributeDefinition> getAncestorKeyAttributeDefinitions() {
		List<AttributeDefinition> result = new ArrayList<AttributeDefinition>();
		List<EntityDefinition> ancestors = parentEntityDefinition.getAncestorEntityDefinitionsInReverseOrder();
		for (EntityDefinition ancestor : ancestors) {
			result.addAll(ancestor.getKeyAttributeDefinitions());
		}
		result.addAll(parentEntityDefinition.getKeyAttributeDefinitions());
		return result;
	}
	
	private int extractAttributePosition(String colName) {
		Matcher matcher = MULTIPLE_ATTRIBUTE_COLUMN_NAME_PATTERN.matcher(colName);
		if (matcher.matches()) {
			String posStr = matcher.group(1);
			int pos = Integer.parseInt(posStr);
			return pos;
		} else {
			return 1;
		}
	}
	
	private FieldDefinition<?> extractFieldDefinition(EntityDefinition parentEntityDefn, String colName) {
		String absoluteColName = getAbsoluteColName(colName);
		
		List<NodeDefinition> childDefns = parentEntityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefns) {
			String childName = childDefn.getName();
			if ( absoluteColName.equals(childName) ) {
				if ( childDefn instanceof AttributeDefinition ) {
					AttributeDefinition attrDefn = (AttributeDefinition) childDefn;
					if (attrDefn.hasMainField()) {
						String mainFieldName = attrDefn.getMainFieldName();
						return attrDefn.getFieldDefinition(mainFieldName);
					} else {
						//it is a composite attribute without a "main" field (date, time, coordinate, taxon)
						return null;
					}
				} else {
					//column name matches an entity name: error
					return null;
				}
			} else if ( absoluteColName.startsWith(childName + ATTRIBUTE_FIELD_SEPARATOR) ) {
				if ( childDefn instanceof EntityDefinition ) {
					if ( childDefn.isMultiple() ) {
						//ignore it
					} else {
						String colNamePart = absoluteColName.substring(childName.length() + ATTRIBUTE_FIELD_SEPARATOR.length());
						FieldDefinition<?> nestedFieldDefn = extractFieldDefinition((EntityDefinition) childDefn, colNamePart);
						if ( nestedFieldDefn != null ) {
							return nestedFieldDefn;
						}
					}
				} else {
					for (FieldDefinition<?> fieldDefn : ((AttributeDefinition) childDefn).getFieldDefinitions()) {
						if ( absoluteColName.equals(childName + ATTRIBUTE_FIELD_SEPARATOR + fieldDefn.getName() ) ) {
							return fieldDefn;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Removes the relative attribute index at the end of the column name
	 */
	private String getAbsoluteColName(String colName) {
		return colName.replaceAll("\\[\\d+\\]$", "");
	}
	
	private List<EntityIdentifierDefinition> getAncestorIdentifiers() {
		List<EntityDefinition> ancestorEntityDefns = parentEntityDefinition.getAncestorEntityDefinitionsInReverseOrder();
		ancestorEntityDefns.add(parentEntityDefinition);
		List<EntityIdentifierDefinition> entityIdentifierDefns = new ArrayList<DataLine.EntityIdentifierDefinition>();
		for (EntityDefinition ancestorEntityDefn : ancestorEntityDefns) {
			EntityIdentifierDefinition identifier;
			if ( ancestorEntityDefn.isMultiple() ) {
				List<AttributeDefinition> keyDefns = ancestorEntityDefn.getKeyAttributeDefinitions();
				if ( keyDefns.isEmpty() ) {
					identifier = new DataLine.EntityPositionIdentifierDefinition(ancestorEntityDefn.getId());
				} else {
					identifier = new DataLine.EntityKeysIdentifierDefintion(ancestorEntityDefn);
				}
			} else {
				identifier = new DataLine.SingleEntityIdentifierDefinition(ancestorEntityDefn.getId());
			}
			entityIdentifierDefns.add(identifier);
		}
		return entityIdentifierDefns;
		
	}
	
	private List<String> getExpectedAncestorKeyColumnNames() {
		List<EntityIdentifierDefinition> entityIdentifierDefns = getAncestorIdentifiers();
		//validate ancestor key columns
		Schema schema = parentEntityDefinition.getSchema();
		List<String> expectedEntityKeyColumns = new ArrayList<String>();
		for (EntityIdentifierDefinition identifier : entityIdentifierDefns) {
			int defnId = identifier.getEntityDefinitionId();
			EntityDefinition defn = (EntityDefinition) schema.getDefinitionById(defnId);
			if ( identifier instanceof EntityPositionIdentifierDefinition ) {
				String expectedColName = getPositionColumnName(defn);
				expectedEntityKeyColumns.add(expectedColName);
			} else if ( identifier instanceof SingleEntityIdentifierDefinition ) {
				//skip
			} else {
				List<AttributeDefinition> keyDefns = defn.getKeyAttributeDefinitions();
				for (AttributeDefinition keyDefn : keyDefns) {
					List<String> expectedColNames = getKeyAttributeColumnNames(parentEntityDefinition, keyDefn);
					expectedEntityKeyColumns.addAll(expectedColNames);
				}
			}
		}
		return expectedEntityKeyColumns;
	}

	class DataCSVLineParser extends CSVLineParser<DataLine> {
		
		DataCSVLineParser(DataCSVReader reader) {
			super(reader, currentCSVLine);
		}
		
		public DataLine parse() throws ParsingException {
			DataLine line = super.parse();
			
			addAncestorIdentifierValues(line);
			
			addFieldValues(line);
			
			return line;
		}

		private void addAncestorIdentifierValues(DataLine line) throws ParsingException {
			Schema schema = parentEntityDefinition.getSchema();
			List<EntityIdentifierDefinition> ancestorIdentifiers = getAncestorIdentifiers();
			for (EntityIdentifierDefinition identifierDefn : ancestorIdentifiers) {
				EntityDefinition entityDefn = (EntityDefinition) schema.getDefinitionById(identifierDefn.getEntityDefinitionId());
				if ( identifierDefn instanceof EntityKeysIdentifierDefintion ) {
					EntityIdentifier<?> identifier = new DataLine.EntityKeysIdentifier((EntityKeysIdentifierDefintion) identifierDefn);
					List<AttributeDefinition> keyDefns = entityDefn.getKeyAttributeDefinitions();
					for (AttributeDefinition keyDefn : keyDefns) {
						Value value = extractValue(line, keyDefn);
						if (value == null) {
							String columnName = keyDefn.hasMainField() ? line.getColumnName(keyDefn.getMainFieldDefinition()): "";
							throw new ParsingException(new ParsingError(ErrorType.INVALID_VALUE, 
									line.getLineNumber(), columnName, ROW_IDENTIFIER_NOT_SPECIFIED_MESSAGE_KEY));
						}
						((EntityKeysIdentifier) identifier).addKeyValue(keyDefn.getId(), value);
					}
					line.addAncestorIdentifier(identifier);
				} else if ( identifierDefn instanceof EntityPositionIdentifierDefinition ) {
					String positionColName = getPositionColumnName(entityDefn);
					int position = getColumnValue(positionColName, true, Integer.class);
					if (position <= 0) {
						throw new ParsingException(new ParsingError(ErrorType.INVALID_VALUE, 
								line.getLineNumber(), positionColName, INVALID_NODE_POSITION_VALUE_MESSAGE_KEY));
					}
					EntityIdentifier<?> identifier = new DataLine.EntityPositionIdentifier((EntityPositionIdentifierDefinition) identifierDefn, position);
					line.addAncestorIdentifier(identifier);
				} else {
					//single entity identifier: no need to identify the entity with a value column
				}
			}
		}

		private Value extractValue(DataLine line, AttributeDefinition keyDefn) throws ParsingException {
			List<String> keyAttrColNames = getKeyAttributeColumnNames(parentEntityDefinition, keyDefn);
			try {
				if (keyDefn.isSingleFieldKeyAttribute()) {
					String colVal = getColumnValue(keyAttrColNames.get(0), false, String.class);
					Value val = keyDefn.createValue(colVal);
					return val;
				} else {
					List<String> fieldValues = new ArrayList<String>(keyAttrColNames.size());
					for (int i = 0; i < keyAttrColNames.size(); i++) {
						String keyAttrColName = keyAttrColNames.get(i);
						String fieldVal = getColumnValue(keyAttrColName, false, String.class);
						fieldValues.add(fieldVal);
					}
					AbstractValue value = keyDefn.createValueFromKeyFieldValues(fieldValues);
					return value == null ? null : value;
				}
			} catch (Exception e) {
				throw new ParsingException(new ParsingError(ErrorType.EMPTY, 
						line.getLineNumber(), keyAttrColNames.toArray(new String[keyAttrColNames.size()])));
			}
		}

		private void addFieldValues(DataLine line) throws ParsingException {
			List<String> colNames = csvLine.getColumnNames();
			List<String> expectedAncestorKeyColumnNames = getExpectedAncestorKeyColumnNames();
			List<String> attrColNames = new ArrayList<String>(colNames);
			attrColNames.removeAll(expectedAncestorKeyColumnNames);
			for (String colName : attrColNames) {
				if (! isIgnoredInDataExport(colName)) {
					String value = getColumnValue(colName, false, String.class);
					FieldValueKey fieldValueKey = extractFieldValueKey(colName);
					line.setFieldValue(fieldValueKey, value);
					line.setColumnNameByField(fieldValueKey, colName);
				}
			}
		}

		protected FieldValueKey extractFieldValueKey(String colName) {
			FieldDefinition<?> fieldDefn = extractFieldDefinition(parentEntityDefinition, colName);
			AttributeDefinition attrDefn = (AttributeDefinition) fieldDefn.getParentDefinition();
			int attrPos = extractAttributePosition(colName);
			FieldValueKey fieldValueKey = new FieldValueKey(attrDefn.getId(), attrPos, fieldDefn.getName());
			return fieldValueKey;
		}

	}
	
	class Validator {
		
		public void validate() throws ParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws ParsingException {
			List<String> colNames = getColumnNames();
			
			List<String> expectedEntityKeyColumns = getExpectedAncestorKeyColumnNames();
			if (! colNames.containsAll(expectedEntityKeyColumns)) {
				ParsingError error = new ParsingError(ErrorType.MISSING_REQUIRED_COLUMNS, 1, 
						(String) null, MISSING_REQUIRED_COLUMNS_MESSAGE_KEY);
				String messageArg = StringUtils.join(expectedEntityKeyColumns, ", ");
				error.setMessageArgs(new String[]{messageArg});
				throw new ParsingException(error);
			}
			
			
			List<String> attributeColumnNames = colNames.subList(expectedEntityKeyColumns.size(), colNames.size());
			
			validateAttributeHeaders(attributeColumnNames);
		}

		protected void validateAttributeHeaders(List<String> colNames)
				throws ParsingException {
			for (int i = 0; i < colNames.size(); i++) {
				String colName = StringUtils.trimToEmpty(colNames.get(i));
				
				FieldDefinition<?> fieldDefn = extractFieldDefinition(parentEntityDefinition, colName);
				if ( fieldDefn == null ) {
					if (! isIgnoredInDataExport(colName)) {
						//attribute definition not found
						ParsingError error = new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName, 
								"csvDataImport.error.fieldDefinitionNotFound");
						throw new ParsingException(error);
					}
				} else {
					AttributeDefinition attrDefn = fieldDefn.getAttributeDefinition();
					if (attrDefn.isMultiple()) {
						Matcher matcher = MULTIPLE_ATTRIBUTE_COLUMN_NAME_PATTERN.matcher(colName);
						if (! matcher.matches()) {
							//node position not specified for a multiple attribute
							ParsingError error = new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName, 
									"csvDataImport.error.nodePositionNotSpecified");
							throw new ParsingException(error);
						}
					}
				}
			}
		}

	}
	
}
