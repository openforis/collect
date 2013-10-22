/**
 * 
 */
package org.openforis.collect.manager.dataimport;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.dataimport.DataLine.EntityIdentifier;
import org.openforis.collect.manager.dataimport.DataLine.EntityIdentifierDefinition;
import org.openforis.collect.manager.dataimport.DataLine.EntityKeysIdentifier;
import org.openforis.collect.manager.dataimport.DataLine.EntityKeysIdentifierDefintion;
import org.openforis.collect.manager.dataimport.DataLine.EntityPositionIdentifierDefinition;
import org.openforis.collect.manager.dataimport.DataLine.SingleEntityIdentifierDefinition;
import org.openforis.collect.manager.referencedataimport.CSVDataImportReader;
import org.openforis.collect.manager.referencedataimport.CSVLineParser;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.referencedataimport.ParsingException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;

/**
 * @author S. Ricci
 *
 */
public class DataCSVReader extends CSVDataImportReader<DataLine> {

	private static final String ATTRIBUTE_FIELD_SEPARATOR = "_";
	private static final String POSITION_COLUMN_FORMAT = "_%s_position";

	private static final String MISSING_REQUIRED_COLUMNS_MESSAGE_KEY = "dataImport.parsingError.missing_required_columns.message";
	

	private EntityDefinition parentEntityDefinition;

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
		Validator validator = new Validator();
		validator.validate();
		return true;
	}
	
	protected static String getKeyAttributeColumnName(
			EntityDefinition parentEntityDefinition,
			AttributeDefinition keyAttrDefn) {
		StringBuilder sb = new StringBuilder();
		NodeDefinition parentDefn = keyAttrDefn.getParentDefinition();
		if ( parentDefn != parentEntityDefinition ) {
			sb.append(parentDefn.getName());
			sb.append("_");
		}
		sb.append(keyAttrDefn.getName());
		return sb.toString();
	}
	
	protected static String getPositionColumnName(EntityDefinition defn) {
		return String.format(POSITION_COLUMN_FORMAT, defn.getName());
	}

	protected static String[] getKeyAttributeColumnNames(EntityDefinition parentEntityDefinition, 
			List<AttributeDefinition> ancestorKeyAttrDefns) {
		String[] result = new String[ancestorKeyAttrDefns.size()];
		for (int i = 0; i < ancestorKeyAttrDefns.size(); i++) {
			AttributeDefinition attrDefn = ancestorKeyAttrDefns.get(i);
			String colName = getKeyAttributeColumnName(parentEntityDefinition, attrDefn);
			result[i] = colName;
		}
		return result;
	}
	
	protected List<AttributeDefinition> getAncestorKeyAttributeDefinitions() {
		List<AttributeDefinition> result = new ArrayList<AttributeDefinition>();
		List<EntityDefinition> ancestors = parentEntityDefinition.getAncestorEntityDefinitions();
		for (EntityDefinition ancestor : ancestors) {
			result.addAll(ancestor.getKeyAttributeDefinitions());
		}
		result.addAll(parentEntityDefinition.getKeyAttributeDefinitions());
		return result;
	}
	
	private AttributeDefinition extractAttributeDefinition(EntityDefinition parentEntityDefn, String colName) {
		List<NodeDefinition> childDefns = parentEntityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefns) {
			String childName = childDefn.getName();
			if ( colName.equals(childName) ) {
				if ( childDefn instanceof AttributeDefinition ) {
					return (AttributeDefinition) childDefn;
				} else {
					//column name matches an entity name: error
					return null;
				}				
			} else if ( colName.startsWith(childName + ATTRIBUTE_FIELD_SEPARATOR) ) {
				if ( childDefn instanceof EntityDefinition ) {
					if ( childDefn.isMultiple() ) {
						//ignore it
					} else {
						String colNamePart = colName.substring(childName.length() + ATTRIBUTE_FIELD_SEPARATOR.length());
						AttributeDefinition nestedAttrDefn = extractAttributeDefinition((EntityDefinition) childDefn, colNamePart);
						if ( nestedAttrDefn != null ) {
							return nestedAttrDefn;
						}
					}
				} else {
					List<FieldDefinition<?>> fieldDefns = ((AttributeDefinition) childDefn).getFieldDefinitions();
					for (FieldDefinition<?> fieldDefn : fieldDefns) {
						if ( colName.equals(childName + ATTRIBUTE_FIELD_SEPARATOR + fieldDefn.getName() ) ) {
							return (AttributeDefinition) childDefn;
						}
					}
				}
			}
		}
		return null;
	}
	
	private FieldDefinition<?> extractFieldDefinition(AttributeDefinition attributeDefinition, String columnName) {
		List<FieldDefinition<?>> fieldDefns = attributeDefinition.getFieldDefinitions();
		for (FieldDefinition<?> fieldDefn : fieldDefns) {
			String fieldName = fieldDefn.getName();
			if ( columnName.endsWith(fieldName) ) {
				return fieldDefn;
			}
		}
		return null;
	}
	
	private List<EntityIdentifierDefinition> getAncestorIdentifiers() {
		List<EntityDefinition> ancestorEntityDefns = parentEntityDefinition.getAncestorEntityDefinitions();
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
					String expectedColName = getKeyAttributeColumnName(parentEntityDefinition, keyDefn);
					expectedEntityKeyColumns.add(expectedColName);
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
			Schema schema = parentEntityDefinition.getSchema();
			List<EntityIdentifierDefinition> ancestorIdentifiers = getAncestorIdentifiers();
			for (EntityIdentifierDefinition identifierDefn : ancestorIdentifiers) {
				EntityDefinition entityDefn = (EntityDefinition) schema.getDefinitionById(identifierDefn.getEntityDefinitionId());
				EntityIdentifier<?> identifier;
				if ( identifierDefn instanceof EntityKeysIdentifierDefintion ) {
					identifier = new DataLine.EntityKeysIdentifier((EntityKeysIdentifierDefintion) identifierDefn);
					List<AttributeDefinition> keyDefns = entityDefn.getKeyAttributeDefinitions();
					for (AttributeDefinition keyDefn : keyDefns) {
						String keyAttrColName = getKeyAttributeColumnName(parentEntityDefinition, keyDefn);
						String value = getColumnValue(keyAttrColName, false, String.class);
						((EntityKeysIdentifier) identifier).addKeyValue(keyDefn.getId(), value);
					}
				} else {
					String positionColName = getPositionColumnName(entityDefn);
					int position = getColumnValue(positionColName, true, Integer.class);
					identifier = new DataLine.EntityPositionIdentifier((EntityPositionIdentifierDefinition) identifierDefn, position);
				}
				line.setAncestorIdentifier(identifier);
			}
			List<String> colNames = csvLine.getColumnNames();
			List<String> expectedAncestorKeyColumnNames = getExpectedAncestorKeyColumnNames();
			List<String> attrColNames = colNames.subList(expectedAncestorKeyColumnNames.size(), colNames.size());
			for (String colName : attrColNames) {
				String value = getColumnValue(colName, false, String.class);
				AttributeDefinition attrDefn = extractAttributeDefinition(parentEntityDefinition, colName);
				FieldDefinition<?> fieldDefn = extractFieldDefinition(attrDefn, colName);
				if ( fieldDefn == null ) {
					fieldDefn = attrDefn.getFieldDefinition(attrDefn.getMainFieldName());
				}
				line.setFieldValue(attrDefn.getId(), fieldDefn.getName(), value);
				line.setColumnNameByField(attrDefn.getId(), fieldDefn.getName(), colName);
			}
			return line;
		}

	}
	
	class Validator {
		
		public void validate() throws ParsingException {
			validateHeaders();
		}

		protected void validateHeaders() throws ParsingException {
			List<String> colNames = getColumnNames();
			List<String> expectedEntityKeyColumns = getExpectedAncestorKeyColumnNames();
			if ( expectedEntityKeyColumns.size() > colNames.size() || 
					!expectedEntityKeyColumns.equals(colNames.subList(0, expectedEntityKeyColumns.size()))) {
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
				AttributeDefinition attrDefn = extractAttributeDefinition(parentEntityDefinition, colName);
				if ( attrDefn == null ) {
					//attribute definition not found
					ParsingError error = new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName);
					throw new ParsingException(error);
				}
			}
		}

	}

}
