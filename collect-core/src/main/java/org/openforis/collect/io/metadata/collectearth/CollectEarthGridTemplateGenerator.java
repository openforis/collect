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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openforis.collect.io.metadata.collectearth.CSVFileValidationResult.ErrorType;
import org.openforis.collect.manager.validation.SurveyValidator.ValidationParameters;
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
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.model.NumberValue;
import org.openforis.idm.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectEarthGridTemplateGenerator  {

	private static final String LONG_COORDINATE = "XCoordinate";

	private static final String LAT_COORDINATE = "YCoordinate";

	private static final String TEST_PLOTS_TEMPLATE_PATH = "org/openforis/collect/designer/templates/collectearth/test_plots.ced.template";
	
	public static final int CSV_LENGTH_ERROR = 4000;
	public static final int CSV_LENGTH_WARNING = 2000;
	private Logger logger = LoggerFactory.getLogger( getClass() );

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
	

	public CSVFileValidationResult validate(File file, CollectSurvey survey, ValidationParameters validationParameters) {
		CsvReader csvReader = null;
		CSVFileValidationResult validationResults = null;
		List<CSVRowValidationResult> rowValidations = new ArrayList<CSVRowValidationResult>();
		try {
			csvReader = new CsvReader(file);
			boolean headersFound = false;
			
			if (csvReader.size() == 1) {
				headersFound = true;
			}else{			
				
				try {
					csvReader.readHeaders();
					List<String> firstLineValues = csvReader.getColumnNames();
					
					headersFound = lineContainsHeaders(survey, firstLineValues);
					if( headersFound ){
						validationResults = validateCSVHeaders(firstLineValues, survey);
					}else{
						
						//Check that the number of columns coincide with the number of attributes expected
						// Get the list of attribute types expected per row!
						List<AttributeDefinition> attributesPerRow = getAttributesPerRow(survey);
						
						// Validate that the number of columns in the CSV and the expected number of columns match!!!!
						if( firstLineValues.size() != attributesPerRow.size() ){
							// The expected number of columns and the actual columns do not fit!!
							// Break the operation and return a validation error!
							validationResults = new CSVFileValidationResult( ErrorType.INVALID_HEADERS, getExpectedHeaders(survey) , firstLineValues);
						}
					}
				} catch(Exception e) {
					//this may happen when there are duplicate values in the first row
					headersFound = false;
//					csvReader.setHeadersRead(true);	
				}
			}
			if( validationResults == null ){

				rowValidations.addAll( validateCsvRows( csvReader , survey, headersFound , validationParameters.isValidateOnlyFirstLines() ) );
				
				long linesRead = csvReader.getLinesRead();
				if( linesRead > CSV_LENGTH_ERROR ){
					validationResults  = new CSVFileValidationResult(ErrorType.INVALID_NUMBER_OF_PLOTS_TOO_LARGE);
					validationResults.setNumberOfRows( (int)linesRead );
				}else if( csvReader.getLinesRead() > CSV_LENGTH_WARNING){
					validationResults  = new CSVFileValidationResult(ErrorType.INVALID_NUMBER_OF_PLOTS_WARNING);
					validationResults.setNumberOfRows( (int)linesRead );
				}
				
				
			}
			
					
		} catch (Exception e) {
			logger.error( "Error reading CSV file ", e);
			validationResults  = new CSVFileValidationResult(ErrorType.INVALID_FILE_TYPE);
		} finally {
			IOUtils.closeQuietly(csvReader);
		}
		
		if( validationResults == null ){
			validationResults = new CSVFileValidationResult();
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

	
	private List<CSVRowValidationResult> validateCsvRows( CsvReader csvReader, CollectSurvey survey, boolean firstLineIsHeaders, boolean validateOnlyFirstLines ) throws IOException {
				
		List<CSVRowValidationResult> results = new ArrayList<CSVRowValidationResult>();
		List<CSVRowValidationResult> validateCsvRow = null;
		// Get the list of attribute types expected per row!
		List<AttributeDefinition> attributesPerRow = getAttributesPerRow(survey);
		
		int rowNumber =1 ;
		
		if (!firstLineIsHeaders){
			validateCsvRow = validateCsvRow(  survey, attributesPerRow, (String[]) csvReader.getColumnNames().toArray( new String[ csvReader.getColumnNames().size() ]), rowNumber++ );
			if( !validateCsvRow.isEmpty() ){
				results.addAll( validateCsvRow  );
			}
		}

				
		CsvLine csvLine;
		
		while( ( csvLine = csvReader.readNextLine() ) != null ){
			if( validateOnlyFirstLines && rowNumber < 50 ){
				validateCsvRow = validateCsvRow(  survey, attributesPerRow, csvLine.getLine(), rowNumber++ );
				if(validateCsvRow!=null){
					results.addAll( validateCsvRow  );
				}
			}
		}	
		
		return results;
		
	}
	
	private List<CSVRowValidationResult> validateCsvRow(CollectSurvey survey, List<AttributeDefinition> attributesPerRow, String[] nextLine,
			int rowNumber) {
		
		List<CSVRowValidationResult> validationColumns = new ArrayList<CSVRowValidationResult>();
		
		int column = 0;
		for( int pos = 0; pos < attributesPerRow.size(); pos++ ){
		
			if( nextLine.length > pos ){
				String message = validateCell( survey, attributesPerRow.get(pos), nextLine[pos] );
				if( message !=null ){
					validationColumns.add( new CSVRowValidationResult(rowNumber+1, ErrorType.INVALID_VALUES_IN_CSV, column, message ));
				} 
			}
			column++;
			
		}
		
		return validationColumns;
		
		
	}

	
	private boolean isEpsg4326SRS(CollectSurvey survey){
		return survey.getSpatialReferenceSystem("EPSG:4326")!=null;
	}
	
	/**
	 * Checks if a value can be used as the input for an attribute 
	 * @param attributeDefinition The attribute that we want to check the value against
	 * @param value The value that should be checked
	 * @return True if the value can be used in the attribute. False otherwise (for instance trying to use a string "abc" as the input for a Number attribute
	 */
	private String validateCell(CollectSurvey survey, AttributeDefinition attributeDefinition, String value) {
		
		try{
			
			// By creating the value using hte attribute definitoon a validation is performed
			Value valueCreated = attributeDefinition.createValue( value );
			
			if( attributeDefinition.isAlwaysRequired() && StringUtils.isBlank( value )){
				return String.format("The attribute %s is marekd as \"always required\". The value in the cell is empty!", attributeDefinition.getLabel( NodeLabel.Type.INSTANCE ));
			}
			
			if ( isEpsg4326SRS(survey) && attributeDefinition.getName().equals(LAT_COORDINATE) ){
				double lat = ( (NumberValue<Number>) valueCreated ).getValue().doubleValue();
				if(lat < -90 || lat > 90 ){
					return "The latitude of a plot must be between -90 and 90 degrees!"; 
				}
			}
			
			if ( isEpsg4326SRS(survey) && attributeDefinition.getName().equals(LONG_COORDINATE) ){
				double longitude = ( (NumberValue<Number>) valueCreated ).getValue().doubleValue();
				if(longitude < -180 || longitude > 180 ){
					return "The latitude of a plot must be between -180 and 180 degrees!"; 
				}
			}
			
			// Make sure that the code used in a code-attribute is actually present on the codelist
			if( attributeDefinition instanceof CodeAttributeDefinition ){
				
				CodeAttributeDefinition cad = (CodeAttributeDefinition) attributeDefinition;
				// IF IT IS A STRICT CODE LIST
				if( !cad.isAllowUnlisted() ){
					
					// Check that the code exists in the codelist
					
					// Gets the level (in case of hierarchical codelists) that the attribute refers to. If it is a flat codelist then it is alway 0
					int levelIndex = cad.getLevelIndex();
					
					CodeListService codeListService = attributeDefinition.getSurvey().getContext().getCodeListService();
					
					List<CodeListItem> items = codeListService.loadItems( cad.getList(), levelIndex+1 );
					
					// Check one by one in the codes of the codelist assigned to the attribute if the value is present as a code!
					for (CodeListItem codeListItem : items) {
						if( codeListItem.getCode().equals(value)){
							// FOUND! All good, return null
							return null;
						}
					}
					
					return String.format("The code with value \"%s\" is not part of the codelist used by code %s ", value, attributeDefinition.getLabel( NodeLabel.Type.INSTANCE ));
					
				}
				
			}else if( attributeDefinition instanceof CodeAttributeDefinition ){
				
			}
		
		}catch(Exception e){
			return String.format("The value \"%s\" cannot be used as a value for the attribute %s", value, attributeDefinition.getLabel( NodeLabel.Type.INSTANCE ) );
		}
		
		return null;
	}

	private CSVFileValidationResult validateCSVHeaders(List<String> headers, CollectSurvey survey) {
		List<String> expectedHeaders = getExpectedHeaders(survey);
		if (headers == null || headers.size() < expectedHeaders.size()) {
			return new CSVFileValidationResult(ErrorType.INVALID_HEADERS, expectedHeaders, headers);
		} else {
			List<String> headersSublist = headers.subList(0, expectedHeaders.size());
			
			if (toLowerCaseList(expectedHeaders).equals(toLowerCaseList(headersSublist))) {
				return null;
			} else {
				return new CSVFileValidationResult(ErrorType.INVALID_HEADERS, expectedHeaders, headers);
			}
		}
	}

	public List<String> getExpectedHeaders(CollectSurvey survey) {	
		List<AttributeDefinition> attributesPerRow = getAttributesPerRow(survey);
		return CollectionUtils.project(attributesPerRow, "name");
	}
	
	private List<AttributeDefinition> getAttributesPerRow(CollectSurvey survey){
				
		List<AttributeDefinition> expectedColumns = new ArrayList<AttributeDefinition>();
		
		List<AttributeDefinition> keyAttributeDefinitions = survey.getSchema().getRootEntityDefinitions().get(0).getKeyAttributeDefinitions();
		expectedColumns.addAll(keyAttributeDefinitions);
		NumberAttributeDefinition latAttribute = survey.getSchema().createNumberAttributeDefinition();
		latAttribute.setType(Type.REAL);
		latAttribute.setName(LAT_COORDINATE);
		latAttribute.setLabel(NodeLabel.Type.INSTANCE, survey.getDefaultLanguage(), "Latitude");
		expectedColumns.add( latAttribute );
		
		NumberAttributeDefinition longAttribute = survey.getSchema().createNumberAttributeDefinition();
		longAttribute.setType(Type.REAL);
		longAttribute.setName(LONG_COORDINATE);
		longAttribute.setLabel(NodeLabel.Type.INSTANCE, survey.getDefaultLanguage(), "Longitude");

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
