package org.openforis.collect.io.metadata.collectearth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.metadata.collectearth.CSVFileValidationResult.ErrorType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Files;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectEarthGridTemplateGeneratorImpl implements CollectEarthGridTemplateGenerator {

	private static final String TEST_PLOTS_TEMPLATE_PATH = "org/openforis/collect/designer/templates/collectearth/test_plots.ced.template";
	
	@Override
	public File generateTemplateCSVFile(CollectSurvey survey) throws IOException {
		//copy the template txt file into a String
		InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_PLOTS_TEMPLATE_PATH);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
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
	
	@Override
	public CSVFileValidationResult validate(File file, CollectSurvey survey) {
		CsvReader csvReader = null;
		try {
			csvReader = new CsvReader(file);
			if (csvReader.size() == 1) {
				//skip validation when there is only one row (e.g. ChangeThisGrid.csv file)
				return new CSVFileValidationResult();
			}
			csvReader.readHeaders();
			List<String> columnNames = csvReader.getColumnNames();
			return validateCSVHeaders(columnNames, survey);
		} catch (Exception e) {
			return new CSVFileValidationResult(ErrorType.INVALID_FILE_TYPE);
		} finally {
			IOUtils.closeQuietly(csvReader);
		}
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
		List<String> expectedHeaders = new ArrayList<String>();
		List<AttributeDefinition> keyAttributeDefinitions = survey.getSchema().getRootEntityDefinitions().get(0).getKeyAttributeDefinitions();
		List<String> keyAttrDefnNames = CollectionUtils.project(keyAttributeDefinitions, "name");
		expectedHeaders.addAll(keyAttrDefnNames);
		expectedHeaders.addAll(Arrays.asList("YCoordinate","XCoordinate"));
		
		List<AttributeDefinition> fromCsvAttributes = survey.getExtendedDataFields();
		List<String> fromCsvAttributeNames = CollectionUtils.project(fromCsvAttributes, "name");
		expectedHeaders.addAll(fromCsvAttributeNames);
		
		return expectedHeaders;
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
