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
	
	private String extractAttributeName(EntityDefinition parentEntityDefn, String colName) {
		String fieldName = extractFieldName(parentEntityDefn, colName);
		if ( fieldName == null ) {
			return colName;
		} else {
			return colName.substring(0, colName.length() - fieldName.length() - 1);
		}
	}
	
	private String extractFieldName(EntityDefinition parentEntityDefn, String colName) {
		int lastIndexOfFieldSeparator = colName.lastIndexOf(ATTRIBUTE_FIELD_SEPARATOR);
		while ( lastIndexOfFieldSeparator > 0 ) {
			String attrName = colName.substring(0, lastIndexOfFieldSeparator);
			if ( parentEntityDefn.containsChildDefinition(attrName) && 
					parentEntityDefn.getChildDefinition(attrName) instanceof AttributeDefinition ) {
				return colName.substring(lastIndexOfFieldSeparator + 1);
			} else {
				lastIndexOfFieldSeparator = attrName.lastIndexOf(ATTRIBUTE_FIELD_SEPARATOR);
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
				String value = getColumnValue(keyAttrColName, true, String.class);
				line.setAncestorKey(keyDefn, value);
			}
			List<String> colNames = csvLine.getColumnNames();
			List<String> attrColNames = colNames.subList(ancestorKeyAttrDefns.size(), colNames.size());
			for (String colName : attrColNames) {
				String value = getColumnValue(colName, true, String.class);
				String attrName = extractAttributeName(parentEntityDefinition, colName);
				AttributeDefinition attrDefn = (AttributeDefinition) parentEntityDefinition.getChildDefinition(attrName);
				String fieldName = extractFieldName(parentEntityDefinition, colName);
				if ( fieldName == null ) {
					fieldName = attrDefn.getMainFieldName();
				}
				FieldDefinition<?> fieldDefn = attrDefn.getFieldDefinition(fieldName);
				line.setFieldValue(fieldDefn, value);
				line.setColumnNameByField(fieldDefn, colName);
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
					error.setMessageArgs(keyAttrExpectedColNames);
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
				boolean wrongColName = false;
				String attrName = extractAttributeName(parentEntityDefinition, colName);
				if ( parentEntityDefinition.containsChildDefinition(attrName) ) {
					NodeDefinition childDefn = parentEntityDefinition.getChildDefinition(attrName);
					if ( childDefn instanceof AttributeDefinition ) {
						String fieldName = extractFieldName(parentEntityDefinition, colName);
						if ( fieldName != null ) {
							FieldDefinition<?> fieldDefn = ((AttributeDefinition) childDefn).getFieldDefinition(fieldName);
							if ( fieldDefn == null ) {
								//field definition not found
								wrongColName = true;
							}
						}
					} else {
						//attribute definition expected
						wrongColName = true;
					}
				} else {
					//node definition not found
					wrongColName = true;
				}
				if ( wrongColName ) {
					ParsingError error = new ParsingError(ErrorType.WRONG_COLUMN_NAME, 1, colName);
					throw new ParsingException(error);
				}
			}
		}

	}

}
