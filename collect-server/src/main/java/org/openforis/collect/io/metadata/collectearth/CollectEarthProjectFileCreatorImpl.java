package org.openforis.collect.io.metadata.collectearth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
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
import org.openforis.collect.earth.core.rdb.RelationalSchemaContext;
import org.openforis.collect.io.metadata.collectearth.balloon.CollectEarthBalloonGenerator;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FileWrapper;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.Zip4jFiles;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final String README_FILE_PATH = "org/openforis/collect/designer/templates/collectearth/README.txt";
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
	private static final double HECTARES_TO_METERS_CONVERSION_FACTOR = 10000d;
	private static final String README_FILE = "README.txt";
	private static final String SAIKU_SCHEMA_PLACEHOLDER = "${saikuDbSchema}";
	
	private CodeListManager codeListManager;
	private Logger logger = LoggerFactory.getLogger( CollectEarthProjectFileCreatorImpl.class);
	
	@Override
	public File create(CollectSurvey survey, String language) throws Exception {
		// create output zip file
		File outputFile = File.createTempFile("openforis-collect-earth-temp", ".zip");
		outputFile.delete(); //prevent exception creating zip file with zip4j
		
		// create placemark
		File placemarkFile = createPlacemark(survey);
		File projectProperties = generateProjectProperties(survey,language);
		File balloon = generateBalloon(survey, language);
		File cube = generateCube(survey, language);
		File kmlTemplate = generateKMLTemplate(survey);
		File testPlotsCSVFile = generateTestPlotsCSVFile(survey);
		File readmeFile = getFileFromResouces(README_FILE_PATH);
		
		ZipFile zipFile = new ZipFile(outputFile);
		
		ZipParameters zipParameters = new ZipParameters();

		// COMP_DEFLATE is for compression
		// COMp_STORE no compression
		zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		// DEFLATE_LEVEL_ULTRA = maximum compression
		zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
		
		Zip4jFiles.addFile(zipFile, projectProperties, PROJECT_PROPERTIES_FILE_NAME, zipParameters);
		Zip4jFiles.addFile(zipFile, placemarkFile, PLACEMARK_FILE_NAME, zipParameters);
		Zip4jFiles.addFile(zipFile, balloon, BALLOON_FILE_NAME, zipParameters);
		Zip4jFiles.addFile(zipFile, cube, CUBE_FILE_NAME, zipParameters);
		Zip4jFiles.addFile(zipFile, kmlTemplate, KML_TEMPLATE_FILE_NAME, zipParameters);
		Zip4jFiles.addFile(zipFile, readmeFile, README_FILE, zipParameters);
		Zip4jFiles.addFile(zipFile, testPlotsCSVFile, TEST_PLOTS_FILE_NAME, zipParameters);
		
		addCodeListImages(zipFile, survey, zipParameters);
		
		includeEarthFiles(zipFile, zipParameters);
		
		return outputFile;
	}

	public File getFileFromResouces( String pathToResource ) throws URISyntaxException {
		InputStream readmeContents = this.getClass().getClassLoader().getResourceAsStream( pathToResource );
		File tempFile = null;
		try {
			tempFile = File.createTempFile("readme", "txt");
			tempFile.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tempFile);
			IOUtils.copy(readmeContents,fos);
			readmeContents.close();
			fos.close();
		} catch (FileNotFoundException e) {
			logger.error("Error finding file " + pathToResource, e);
		} catch (IOException e) {
			logger.error("Error copying file " + pathToResource, e);
		}
		return tempFile;
	}

	private void includeEarthFiles(ZipFile zipFile, ZipParameters zipParameters)
			throws IOException, ZipException {
		Resource[] earthFileResources = new PathMatchingResourcePatternResolver().getResources(EARTH_FILES_RESOURCE_PATH + "**");
		for (Resource resource : earthFileResources) {
			if (resource.exists() && resource.isReadable() && StringUtils.isNotBlank(resource.getFilename())) {
				String path = ((ClassPathResource) resource).getPath();
				String relativePath = StringUtils.removeStart(path, EARTH_FILES_RESOURCE_PATH);
				Zip4jFiles.addFile(zipFile, resource.getInputStream(), EARTH_FILES_FOLDER_NAME + "/" + relativePath, zipParameters);
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

	private File generateProjectProperties(CollectSurvey survey, String language) throws IOException {
		Properties p = new Properties();
		p.put("survey_name", survey.getName());
		p.put("balloon", "${project_path}/balloon.html");
		p.put("metadata_file", "${project_path}/placemark.idm.xml");
		p.put("template", "${project_path}/kml_template.fmt");
		p.put("csv", "${project_path}/test_plots.ced");
		p.put("sample_shape", "SQUARE");
		p.put("distance_between_sample_points", String.valueOf(calculateDistanceBetweenSamplePoints(survey)));
		p.put("distance_to_plot_boundaries", String.valueOf(calculateFrameDistance(survey)));
		p.put("number_of_sampling_points_in_plot", String.valueOf(survey.getAnnotations().getCollectEarthSamplePoints()));
		p.put("inner_point_side", "2");
		p.put("open_bing_maps", "true");
		p.put("open_earth_engine", "true");
		p.put("open_here_maps", "true");
		p.put("open_gee_playground", "true");
		p.put("db_driver", "SQLITE");
		p.put("use_browser", "chrome");
		p.put("ui_language", language);

		File file = File.createTempFile("collect-earth-project", ".properties");
		FileWriter writer = new FileWriter(file);
		p.store(writer, null);
		return file;
	}

	private int calculateFrameDistance(CollectSurvey survey) {
		CollectAnnotations annotations = survey.getAnnotations();
		double plotWidth = Math.sqrt(annotations.getCollectEarthPlotArea() * HECTARES_TO_METERS_CONVERSION_FACTOR);
		int samplePoints = annotations.getCollectEarthSamplePoints();
		if (samplePoints == 0) {
			return Double.valueOf(Math.floor((double) (plotWidth / 2))).intValue();
		}
		double pointsPerSide = Math.sqrt(samplePoints);
		int frameDistance = Double.valueOf(Math.floor((double) ((plotWidth / pointsPerSide) / 2))).intValue(); 
		return frameDistance;
	}
		
	private int calculateDistanceBetweenSamplePoints(CollectSurvey survey) {
		CollectAnnotations annotations = survey.getAnnotations();
		
		double plotWidth = Math.sqrt(annotations.getCollectEarthPlotArea() * HECTARES_TO_METERS_CONVERSION_FACTOR);
		int samplePoints = annotations.getCollectEarthSamplePoints();
		if (samplePoints <= 1) {
			return 0;
		}
		double pointsPerWidth = Math.sqrt(samplePoints);
		int frameDistance = calculateFrameDistance(survey); 
		int distanceInMeters = Double.valueOf(Math.floor((double) ((plotWidth - (frameDistance * 2)) / ( pointsPerWidth - 1 ) ))).intValue();
		return distanceInMeters;
	}

	private File generateBalloon(CollectSurvey survey, String language) throws IOException {
		CollectEarthBalloonGenerator generator = new CollectEarthBalloonGenerator(survey, language);
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
		List<AttributeDefinition> fromCsvAttributes = getExtendedDataFields(survey);
		
		//write the dynamic content to be replaced into the template
		String nameOfField = "extraColumns";
		StringBuffer extraHolders = addExtraDataHolders(fromCsvAttributes, nameOfField);
		
		nameOfField = "idColumns";
		List<AttributeDefinition> keyAttributeDefinitions = survey.getSchema().getRootEntityDefinitions().get(0).getKeyAttributeDefinitions();
		extraHolders.append( addExtraDataHolders( keyAttributeDefinitions, nameOfField) );
		
		String content = templateContent.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_CSV_DATA, extraHolders.toString());
		return Files.writeToTempFile(content, "collect-earth-project-file-creator", ".xml");
	}

	public StringBuffer addExtraDataHolders(
			List<AttributeDefinition> fromCsvAttributes, String nameOfField) {
		int extraInfoIndex = 0;
		StringBuffer sb = new StringBuffer();
		for (AttributeDefinition attrDef : fromCsvAttributes) {
			
			String attrName = attrDef.getName();
			sb.append("<Data name=\"EXTRA_" + attrName + "\">\n");
			String value;
			value = "${placemark."+ nameOfField + "[" + extraInfoIndex + "]}";
			extraInfoIndex ++;
			
			sb.append("<value>");
			sb.append(value);
			sb.append("</value>\n");
		    sb.append("</Data>\n");
		}
		return sb;
	}

	/**
	 * Goes though the attributes on the survey finding those that are marked as being key attributes or that are coming "From CSV" meaning that the popup-up will not show the attributes and they will be kept as hidden inputs
	 * @param survey
	 * @return The list of attributes that are marked as coming "From CSV" or that are key attributes
	 */
	private List<AttributeDefinition> getExtendedDataFields(CollectSurvey survey) {
		final CollectAnnotations annotations = survey.getAnnotations();
		final List<AttributeDefinition> fromCsvAttributes = new ArrayList<AttributeDefinition>();
		survey.getSchema().traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				if (def instanceof AttributeDefinition) {
					AttributeDefinition attrDef = (AttributeDefinition) def;
					if (annotations.isFromCollectEarthCSV(attrDef)
														
					) {
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
		List<AttributeDefinition> fromCsvAttributes = getExtendedDataFields(survey);
		
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

	public String getDummyValue(AttributeDefinition attrDef, Integer ord) {
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
	
	private File generateCube(CollectSurvey survey, String language) throws IOException {
		MondrianCubeGenerator cubeGenerator = new MondrianCubeGenerator(survey, language, SAIKU_SCHEMA_PLACEHOLDER, new RelationalSchemaContext().getRdbConfig());
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
					
					Zip4jFiles.addFile(zipFile, imageFile, zipImageFileName, zipParameters);
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