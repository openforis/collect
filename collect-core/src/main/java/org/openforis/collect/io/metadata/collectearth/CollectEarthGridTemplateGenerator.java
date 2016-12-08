package org.openforis.collect.io.metadata.collectearth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openforis.collect.io.metadata.collectearth.CSVFileValidationResult.ErrorType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Files;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectEarthGridTemplateGenerator  {

	private static final String TEST_PLOTS_TEMPLATE_PATH = "org/openforis/collect/designer/templates/collectearth/test_plots.ced.template";

	public File generateTemplateCSVFile(CollectSurvey survey) throws IOException {
		InputStream streamFileWithCsv = getClass().getClassLoader().getResourceAsStream(TEST_PLOTS_TEMPLATE_PATH);
		return generateTemplateCSVFile(survey, streamFileWithCsv);
	}

	

	public File generateTemplateCSVFile(CollectSurvey survey, InputStream streamFileWithCsv) throws IOException {
		//copy the template txt file into a String
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(streamFileWithCsv, writer, "UTF-8");
		String templateContent = writer.toString();
		
		//find "fromCSV" attributes
		List<AttributeDefinition> fromCsvAttributes = survey.getExtendedDataFields();
		
		//write the dynamic content to be replaced into the template
		StringBuffer headerSB = new StringBuffer();
		StringBuffer valuesSB = new StringBuffer();
		for (AttributeDefinition attrDef : fromCsvAttributes) {
			String attrName = attrDef.getName();
			headerSB.append(",\"" + attrName + "\"");
			String value = getDummyValue(attrDef,null);
			valuesSB.append(",\"").append(value).append("\"");
		}
		String content = templateContent.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_COLUMNS_HEADER, headerSB.toString());
		content = content.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_COLUMNS_VALUES, valuesSB.toString());
		
		List<AttributeDefinition> keyAttributeDefinitions = survey.getSchema().getRootEntityDefinitions().get(0).getKeyAttributeDefinitions();
		String keyAttributes = "";
		for (AttributeDefinition keyAttributeDefinition : keyAttributeDefinitions) {
			keyAttributes += keyAttributeDefinition.getName() + ",";
		}
		keyAttributes = keyAttributes.substring(0, keyAttributes.lastIndexOf(",") );		
		content = content.replace(CollectEarthProjectFileCreator.PLACEHOLDER_ID_COLUMNS_HEADER, keyAttributes);
		
		for( int i=1; i<=15;i++){
			String keyValues = "";
			for (AttributeDefinition keyAttributeDefinition : keyAttributeDefinitions) {
				String value = getDummyValue(keyAttributeDefinition,i);
				keyValues += value + ",";
			}
			
			String replaceIdsLine = CollectEarthProjectFileCreator.PLACEHOLDER_ID_COLUMNS_VALUES + "_" + i + ",";
			content = content.replace(replaceIdsLine, keyValues );
		}
		return Files.writeToTempFile(content, "collect-earth-project-file-creator", ".ced");
	}
	

	public CSVFileValidationResult validate(File file, CollectSurvey survey) {
		CsvReader csvReader = null;
		CSVFileValidationResult validationResults = new CSVFileValidationResult();
		List<CSVRowValidationResult> rowValidations = new ArrayList<CSVRowValidationResult>();
		try {
			csvReader = new CsvReader(file);
			if (csvReader.size() == 1) {
				//skip validation when there is only one row (e.g. ChangeThisGrid.csv file)
				return validationResults;
			}
			boolean headersFound = false;
			try {
				csvReader.readHeaders();
				List<String> firstLineValues = csvReader.getColumnNames();
				
				headersFound = lineContainsHeaders(survey, firstLineValues);
				if( headersFound ){
					validationResults = validateCSVHeaders(firstLineValues, survey);
				}
			} catch(Exception e) {
				//this may happen when there are duplicate values in the first row
				headersFound = false;
			}
			
			rowValidations.addAll( validateCsvRows( csvReader , survey, headersFound ) );
					
		} catch (Exception e) {
			validationResults  = new CSVFileValidationResult(ErrorType.INVALID_FILE_TYPE);
		} finally {
			IOUtils.closeQuietly(csvReader);
		}
		validationResults.setRowValidations( rowValidations );
		return validationResults;
	}
	
	private boolean lineContainsHeaders(CollectSurvey survey, List<String> firstLineValues) {
		// The CSV files should contain first the headers of the KEY ATTRIBUTES of the survey, then the latitude and longitude column!
		// If the latitude and longitude columns are not numbers, then this means that the line is a column header, otherwise it is just sample data!
		List<AttributeDefinition> keyAttributeDefinitions = survey.getSchema().getRootEntityDefinitions().get(0).getKeyAttributeDefinitions();
		int headingColumns = keyAttributeDefinitions.size();
		
		// The line contains headers if the second or third columns are NOT number ( the CSV columns should always contain a latitude and a longitude value at those positions)
		return !( NumberUtils.isNumber( firstLineValues.get( headingColumns ) ) && NumberUtils.isNumber( firstLineValues.get(headingColumns + 1 ) ) );
	}

	
	private List<CSVRowValidationResult> validateCsvRows( CsvReader csvReader, CollectSurvey survey, boolean firstLineIsHeaders ) throws IOException {
				
		List<CSVRowValidationResult> results = new ArrayList<CSVRowValidationResult>();
		int rowNumber = firstLineIsHeaders ? 2 : 1 ;
		
		// Get the list of attribute types expected per row!
		List<AttributeDefinition> attributesPerRow = getAttributesPerRow(survey);
				
		CsvLine csvLine = null;
		
		if( !firstLineIsHeaders ){
			csvReader.setHeadersRead(true);
		}
		
		while( ( csvLine = csvReader.readNextLine() ) != null ){
			

			// Validate that the number of columns in the CSV and the expected number of columns match!!!!
			if( csvLine.getLine().length != attributesPerRow.size() ){
				// The excted number of columns and the actual columns do not fit!!
				// Break the operation and return a validation error!
				CSVRowValidationResult columnsMissing = new CSVRowValidationResult( rowNumber , ErrorType.INVALID_NUMBER_OF_COLUMNS);
				columnsMissing.setExpectedColumns( determineExpectedHeaders(survey) );
				break;
			}
			
			CSVRowValidationResult validateCsvRow = validateCsvRow(  survey, attributesPerRow, csvLine, rowNumber );
			if( validateCsvRow != null){
				results.add( validateCsvRow);
			}
			
			rowNumber++;
			
		}	
		
		return results;
		
	}
	
	private CSVRowValidationResult validateCsvRow(CollectSurvey survey, List<AttributeDefinition> attributesPerRow, CsvLine nextLine,
			int rowNumber) {
		
		for( int pos = 0; pos < attributesPerRow.size(); pos++ ){
		
			String message = validateCell( survey, attributesPerRow.get(pos), nextLine.getLine()[pos] );
			if( message !=null ){
				CSVRowValidationResult validation =  new CSVRowValidationResult( rowNumber, ErrorType.INVALID_CONTENT_IN_LINE );
				validation.setMessage(message);
				return validation;
			}
			
		}
		
		return null;
		
		
	}

	private String validateCell(CollectSurvey survey,
			AttributeDefinition attributeDefinition, String string) {
		// TODO Auto-generated method stub
		return null;
	}

	private CSVFileValidationResult validateCSVHeaders(List<String> headers, CollectSurvey survey) {
		List<String> expectedHeaders = determineExpectedHeaders(survey);
		if (headers == null || headers.size() < expectedHeaders.size()) {
			return new CSVFileValidationResult(ErrorType.INVALID_HEADERS, expectedHeaders, headers);
		} else {
			List<String> headersSublist = headers.subList(0, expectedHeaders.size());
			
			if (toLowerCaseList(expectedHeaders).equals(toLowerCaseList(headersSublist))) {
				return new CSVFileValidationResult();
			} else {
				return new CSVFileValidationResult(ErrorType.INVALID_HEADERS, expectedHeaders, headers);
			}
		}
	}

	public List<String> determineExpectedHeaders(CollectSurvey survey) {	
		List<AttributeDefinition> attributesPerRow = getAttributesPerRow(survey);
		return CollectionUtils.project(attributesPerRow, "name");
	}
	
	private List<AttributeDefinition> getAttributesPerRow(CollectSurvey survey){
				
		List<AttributeDefinition> expectedColumns = new ArrayList<AttributeDefinition>();
		
		List<AttributeDefinition> keyAttributeDefinitions = survey.getSchema().getRootEntityDefinitions().get(0).getKeyAttributeDefinitions();
		expectedColumns.addAll(keyAttributeDefinitions);
		NumberAttributeDefinition latAttribute = survey.getSchema().createNumberAttributeDefinition();
		latAttribute.setType(Type.REAL);
		latAttribute.setName("YCoordinate");
		expectedColumns.add( latAttribute );
		
		NumberAttributeDefinition longAttribute = survey.getSchema().createNumberAttributeDefinition();
		longAttribute.setType(Type.REAL);
		longAttribute.setName("XCoordinate");
		expectedColumns.add( longAttribute );
		
		List<AttributeDefinition> fromCsvAttributes = survey.getExtendedDataFields();
		expectedColumns.addAll(fromCsvAttributes);
		
		return expectedColumns;
	}
	
	private String getDummyValue(AttributeDefinition attrDef, Integer ord) {
		String attrName = attrDef.getName();
		
		String value;
		if (attrDef instanceof NumericAttributeDefinition || 
				attrDef instanceof BooleanAttributeDefinition) {
			value = "0";
			if( ord!=null){
				value = ord + "";
			}
		} else if (attrDef instanceof DateAttributeDefinition) {
			value = "1/1/2000";
		} else if (attrDef instanceof CodeAttributeDefinition) {
			CodeListItem firstAvailableItem = getFirstAvailableCodeItem(attrDef);
			value = firstAvailableItem == null ? "0": firstAvailableItem.getCode();
		} else {
			value = "value_" + attrName;
			if( ord != null ){
				value += "_"+ord;
			}
		}
		return value;
	}
	
	private CodeListItem getFirstAvailableCodeItem(AttributeDefinition attrDef) {
		CodeAttributeDefinition codeDefn = (CodeAttributeDefinition) attrDef;
		CodeList list = codeDefn.getList();
		CodeListService codeListService = attrDef.getSurvey().getContext().getCodeListService();
		Integer levelIndex = codeDefn.getListLevelIndex();
		int levelPosition = levelIndex == null ? 1: levelIndex + 1;
		List<CodeListItem> items;
		if (levelPosition == 1) {
			items = codeListService.loadRootItems(list);
		} else {
			items = codeListService.loadItems(list, levelPosition);
		}
		if (items.isEmpty()) {
			return null;
		} else {
			return items.get(0);
		}
	}
	
	private List<String> toLowerCaseList(List<String> list) {
		List<String> result = new ArrayList<String>(list);
		ListIterator<String> iterator = result.listIterator();
		while (iterator.hasNext()) {
			iterator.set(iterator.next().toLowerCase(Locale.ENGLISH));
		}
		return result;
	}


}
