package org.openforis.collect.io.metadata.collectearth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.metadata.collectearth.balloon.CollectEarthBalloonGenerator;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FileWrapper;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.ZipFiles;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CollectEarthProjectFileCreatorImpl implements CollectEarthProjectFileCreator{

	private static final String EARTH_FILES_RESOURCE_PATH = "org/openforis/collect/designer/templates/collectearth/earthFiles/";
	private static final String EARTH_FILES_FOLDER_NAME = "earthFiles";
	private static final String KML_TEMPLATE_PATH = "org/openforis/collect/designer/templates/collectearth/kml_template.txt";
	private static final String TEST_PLOTS_TEMPLATE_PATH = "org/openforis/collect/designer/templates/collectearth/test_plots.ced.template";
	private static final String PLACEMARK_FILE_NAME = "placemark.idm.xml";
	private static final String BALLOON_FILE_NAME = "balloon.html";
	private static final String KML_TEMPLATE_FILE_NAME = "kml_template.fmt";
	private static final String TEST_PLOTS_FILE_NAME = "test_plots.ced";
	private static final String CUBE_FILE_NAME = "collectEarthCubes.xml.fmt";
	private static final String PROJECT_PROPERTIES_FILE_NAME = "project_definition.properties";
	
	private CodeListManager codeListManager;
	
	@Override
	public File create(CollectSurvey survey) throws Exception {
		// create output zip file
		File outputFile = File.createTempFile("openforis-collect-earth-temp", ".zip");
		outputFile.delete(); //prevent exception creating zip file with zip4j
		
		// create placemark
		File placemarkFile = createPlacemark(survey);
		File projectProperties = generateProjectProperties(survey);
		File balloon = generateBalloon(survey);
		File cube = generateCube(survey);
		File kmlTemplate = generateKMLTemplate(survey);
		File testPlotsCSVFile = generateTestPlotsCSVFile(survey);
		
		ZipFile zipFile = new ZipFile(outputFile);
		
		ZipParameters zipParameters = new ZipParameters();

		// COMP_DEFLATE is for compression
		// COMp_STORE no compression
		zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		// DEFLATE_LEVEL_ULTRA = maximum compression
		zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
		
		ZipFiles.addFile(zipFile, projectProperties, PROJECT_PROPERTIES_FILE_NAME, zipParameters);
		ZipFiles.addFile(zipFile, placemarkFile, PLACEMARK_FILE_NAME, zipParameters);
		ZipFiles.addFile(zipFile, balloon, BALLOON_FILE_NAME, zipParameters);
		ZipFiles.addFile(zipFile, cube, CUBE_FILE_NAME, zipParameters);
		ZipFiles.addFile(zipFile, kmlTemplate, KML_TEMPLATE_FILE_NAME, zipParameters);
		ZipFiles.addFile(zipFile, testPlotsCSVFile, TEST_PLOTS_FILE_NAME, zipParameters);
		
		addCodeListImages(zipFile, survey, zipParameters);
		
		includeEarthFiles(zipFile, zipParameters);
		
		return outputFile;
	}

	private void includeEarthFiles(ZipFile zipFile, ZipParameters zipParameters)
			throws IOException, ZipException {
		Resource[] earthFileResources = new PathMatchingResourcePatternResolver().getResources(EARTH_FILES_RESOURCE_PATH + "**");
		for (Resource resource : earthFileResources) {
			if (resource.exists() && resource.isReadable() && StringUtils.isNotBlank(resource.getFilename())) {
				String path = ((ClassPathResource) resource).getPath();
				String relativePath = StringUtils.removeStart(path, EARTH_FILES_RESOURCE_PATH);
				ZipFiles.addFile(zipFile, resource.getInputStream(), EARTH_FILES_FOLDER_NAME + "/" + relativePath, zipParameters);
			}
		}
	}
	
	private File createPlacemark(CollectSurvey survey) throws IOException {
		File file = File.createTempFile("collect-earth-placemark.idm", ".xml");
		FileOutputStream os = new FileOutputStream(file);
		CollectSurveyIdmlBinder binder = new CollectSurveyIdmlBinder(survey.getContext());
		try {
			binder.marshal(survey, os, true, true, false);
		} finally {
			IOUtils.closeQuietly(os);
		}
		return file;
	}

	private File generateProjectProperties(CollectSurvey survey) throws IOException {
		Properties p = new Properties();
		p.put("survey_name", survey.getName());
		p.put("balloon", "${project_path}/balloon.html");
		p.put("metadata_file", "${project_path}/placemark.idm.xml");
		p.put("template", "${project_path}/kml_template.fmt");
		p.put("csv", "${project_path}/test_plots.ced");
		p.put("sample_shape", "SQUARE");
		p.put("distance_between_sample_points", "20");
		p.put("distance_to_plot_boundaries", "10");
		p.put("number_of_sampling_points_in_plot", "25");
		p.put("inner_point_side", "2");
		p.put("open_bing_maps", "true");
		p.put("open_earth_engine", "true");
		p.put("open_here_maps", "true");
		p.put("open_gee_playground", "true");
		p.put("db_driver", "SQLITE");
		p.put("use_browser", "chrome");
		p.put("ui_language", survey.getDefaultLanguage());

		File file = File.createTempFile("collect-earth-project", ".properties");
		FileWriter writer = new FileWriter(file);
		p.store(writer, null);
		return file;
	}

	private File generateBalloon(CollectSurvey survey) throws IOException {
		CollectEarthBalloonGenerator generator = new CollectEarthBalloonGenerator(survey);
		String html = generator.generateHTML();
		return Files.writeToTempFile(html, "collect-earth-project-file-creator", ".html");
	}
	
	private File generateKMLTemplate(CollectSurvey survey) throws IOException {
		//copy the template txt file into a String
		InputStream is = getClass().getClassLoader().getResourceAsStream(KML_TEMPLATE_PATH);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		String templateContent = writer.toString();
		
		//find "fromCSV" attributes
		List<AttributeDefinition> fromCsvAttributes = getFromCSVAttributes(survey);
		
		//write the dynamic content to be replaced into the template
		StringBuffer sb = new StringBuffer();
		int extraInfoIndex = 0;
		for (AttributeDefinition attrDef : fromCsvAttributes) {
			String attrName = attrDef.getName();
			sb.append("<Data name=\"" + attrName + "\">\n");
			String value;
			value = "${placemark.extraColumns[" + extraInfoIndex + "]}";
			extraInfoIndex ++;
			
			sb.append("<value>");
			sb.append(value);
			sb.append("</value>\n");
		    sb.append("</Data>\n");
		    
		}
		String content = templateContent.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_CSV_DATA, sb.toString());
		return Files.writeToTempFile(content, "collect-earth-project-file-creator", ".xml");
	}

	private List<AttributeDefinition> getFromCSVAttributes(CollectSurvey survey) {
		final CollectAnnotations annotations = survey.getAnnotations();
		final List<AttributeDefinition> fromCsvAttributes = new ArrayList<AttributeDefinition>();
		survey.getSchema().traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				if (def instanceof AttributeDefinition) {
					AttributeDefinition attrDef = (AttributeDefinition) def;
					if (annotations.isFromCollectEarthCSV(attrDef)) {
						fromCsvAttributes.add(attrDef);
					}
				}
			}
		});
		return fromCsvAttributes;
	}
	
	private File generateTestPlotsCSVFile(CollectSurvey survey) throws IOException {
		//copy the template txt file into a String
		InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_PLOTS_TEMPLATE_PATH);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		String templateContent = writer.toString();
		
		//find "fromCSV" attributes
		List<AttributeDefinition> fromCsvAttributes = getFromCSVAttributes(survey);
		
		//write the dynamic content to be replaced into the template
		StringBuffer headerSB = new StringBuffer();
		StringBuffer valuesSB = new StringBuffer();
		for (AttributeDefinition attrDef : fromCsvAttributes) {
			String attrName = attrDef.getName();
			headerSB.append(",\"" + attrName + "\"");
			String value;
			if (attrDef instanceof NumericAttributeDefinition || 
					attrDef instanceof BooleanAttributeDefinition) {
				value = "0";
			} else if (attrDef instanceof DateAttributeDefinition) {
				value = "1/1/2000";
			} else if (attrDef instanceof CodeAttributeDefinition) {
				CodeListItem firstAvailableItem = getFirstAvailableCodeItem(attrDef);
				value = firstAvailableItem == null ? "0": firstAvailableItem.getCode();
			} else {
				value = "value_" + attrName;
			}
			valuesSB.append(",\"").append(value).append("\"");
		}
		String content = templateContent.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_COLUMNS_HEADER, headerSB.toString());
		content = content.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_COLUMNS_VALUES, valuesSB.toString());
		return Files.writeToTempFile(content, "collect-earth-project-file-creator", ".ced");
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
	
	private File generateCube(CollectSurvey survey) throws IOException {
		MondrianCubeGenerator cubeGenerator = new MondrianCubeGenerator(survey);
		String xmlSchema = cubeGenerator.generateXMLSchema();
		return Files.writeToTempFile(xmlSchema, "collect-earth-project-file-creator", ".xml");
	}

	private void addCodeListImages(ZipFile zipFile, CollectSurvey survey, ZipParameters zipParameters) throws FileNotFoundException, IOException, ZipException {
		List<CodeList> codeLists = survey.getCodeLists();
		for (CodeList codeList : codeLists) {
			Stack<CodeListItem> stack = new Stack<CodeListItem>();
			List<CodeListItem> rootItems = codeListManager.loadRootItems(codeList);
			stack.addAll(rootItems);
			while (! stack.isEmpty()) {
				CodeListItem item = stack.pop();
				if (item.hasUploadedImage()) {
					FileWrapper imageFileWrapper = codeListManager.loadImageContent((PersistedCodeListItem) item);
					byte[] content = imageFileWrapper.getContent();
					
					File imageFile = copyToTempFile(content, item.getImageFileName());
					
					String zipImageFileName = getCodeListImageFilePath(item);
					
					ZipFiles.addFile(zipFile, imageFile, zipImageFileName, zipParameters);
				}
				List<CodeListItem> childItems = codeListManager.loadChildItems(item);
				for (CodeListItem childItem : childItems) {
					stack.push(childItem);
				}
			}
		}
	}

	public static String getCodeListImageFilePath(CodeListItem item) {
		CodeList codeList = item.getCodeList();
		@SuppressWarnings("unchecked")
		String zipImageFileName = StringUtils.join(Arrays.asList(
				EARTH_FILES_FOLDER_NAME, "img", "code_list", codeList.getId(), item.getId(), item.getImageFileName()), "/");
		return zipImageFileName;
	}

	private File copyToTempFile(byte[] content, String fileName) throws IOException, FileNotFoundException {
		File imageFile = File.createTempFile("collect-earth-project-file-creator", fileName);
		FileOutputStream fos = new FileOutputStream(imageFile);
		fos.write(content);
		fos.close();
		return imageFile;
	}

	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
}