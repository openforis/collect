/**
 * 
 */
package org.openforis.collect.manager.dataimport;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.referencedataimport.CSVDataImportReader;
import org.openforis.collect.manager.referencedataimport.CSVLineParser;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ParsingError.ErrorType;
import org.openforis.collect.manager.referencedataimport.ParsingException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author S. Ricci
 *
 */
public class DataCSVReader extends CSVDataImportReader<DataLine> {

	private static final String ATTRIBUTE_FIELD_SEPARATOR = "_";

	private static final String MISSING_REQUIRED_COLUMNS_MESSAGE_KEY = "dataImport.parsingError.missing_required_columns.message";

	private EntityDefinition parentEntityDefinition;

	public DataCSVReader(Reader reader, EntityDefinition parentEntityDefinition) throws IOException, ParsingException {
		super(reader);
		this.parentEntityDefinition = parentEntityDefinition;
		validateAllFile();
	}
	
	@Override
	protected void init() throws IOException, ParsingException {
		csvReader.readHeaders();
		//postpone file validation
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
	
	class DataCSVLineParser extends CSVLineParser<DataLine> {
		
		DataCSVLineParser(DataCSVReader reader) {
			super(reader, currentCSVLine);
		}
		
		public DataLine parse() throws ParsingException {
			DataLine line = super.parse();
			List<AttributeDefinition> ancestorKeyAttrDefns = getAncestorKeyAttributeDefinitions();
			for (AttributeDefinition keyDefn : ancestorKeyAttrDefns) {
				String keyAttrColName = getKeyAttributeColumnName(parentEntityDefinition, keyDefn);
				String value = getColumnValue(keyAttrColName, false, String.class);
				line.setAncestorKey(keyDefn, value);
			}
			List<String> colNames = csvLine.getColumnNames();
			List<String> attrColNames = colNames.subList(ancestorKeyAttrDefns.size(), colNames.size());
			for (String colName : attrColNames) {
				String value = getColumnValue(colName, false, String.class);
				AttributeDefinition attrDefn = extractAttributeDefinition(parentEntityDefinition, colName);;
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
			List<AttributeDefinition> ancestorKeyAttrDefns = new ArrayList<AttributeDefinition>();
			List<EntityDefinition> ancestorEntityDefns = parentEntityDefinition.getAncestorEntityDefinitions();
			for (EntityDefinition ancestorEntityDefn : ancestorEntityDefns) {
				ancestorKeyAttrDefns.addAll(ancestorEntityDefn.getKeyAttributeDefinitions());
			}
			ancestorKeyAttrDefns.addAll(parentEntityDefinition.getKeyAttributeDefinitions());
			//validate ancestor key columns
			for (int i = 0; i < ancestorKeyAttrDefns.size() && i < colNames.size(); i++) {
				String colName = StringUtils.trimToEmpty(colNames.get(i));
				AttributeDefinition ancestorKeyAttrDefn = ancestorKeyAttrDefns.get(i);
				String expectedColName = getKeyAttributeColumnName(parentEntityDefinition, ancestorKeyAttrDefn);
				if ( ! colName.equals(expectedColName) ) {
					ParsingError error = new ParsingError(ErrorType.MISSING_REQUIRED_COLUMNS, 1, 
							(String) null, MISSING_REQUIRED_COLUMNS_MESSAGE_KEY);
					String[] keyAttrExpectedColNames = getKeyAttributeColumnNames(parentEntityDefinition, ancestorKeyAttrDefns);
					String messageArg = StringUtils.join(keyAttrExpectedColNames, ", ");
					error.setMessageArgs(new String[]{messageArg});
					throw new ParsingException(error);
				}
			}
			validateAttributeHeaders(colNames, ancestorKeyAttrDefns);
		}

		protected void validateAttributeHeaders(List<String> colNames,
				List<AttributeDefinition> ancestorKeyAttrDefns)
				throws ParsingException {
			for (int i = ancestorKeyAttrDefns.size(); i < colNames.size(); i++) {
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
