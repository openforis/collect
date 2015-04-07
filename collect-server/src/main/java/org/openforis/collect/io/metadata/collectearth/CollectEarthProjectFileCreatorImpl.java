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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.metadata.collectearth.balloon.CollectEarthBalloonGenerator;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.ZipFiles;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CollectEarthProjectFileCreatorImpl implements CollectEarthProjectFileCreator{

	private static final String EARTH_FILES_ZIP_FILE_PATH = "org/openforis/collect/designer/templates/collectearth/earth-files-1_0.zip";
	private static final String KML_TEMPLATE_PATH = "org/openforis/collect/designer/templates/collectearth/kml_template.txt";
	private static final String TEST_PLOTS_TEMPLATE_PATH = "org/openforis/collect/designer/templates/collectearth/test_plots.ced.template";
	private static final String PLACEMARK_FILE_NAME = "placemark.idm.xml";
	private static final String BALLOON_FILE_NAME = "balloon.html";
	private static final String KML_TEMPLATE_FILE_NAME = "kml_template.fmt";
	private static final String TEST_PLOTS_FILE_NAME = "test_plots.ced";
	private static final String CUBE_FILE_NAME = "collectEarthCubes.xml.fmt";
	private static final String PROJECT_PROPERTIES_FILE_NAME = "project_definition.properties";
	
	private static final Set<String> FIXED_CSV_ATTRIBUTES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"elevation", "aspect", "slope")));
	
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
		
		// include earthFiles assets folder (js, css, etc.)
		File earthFilesZip = getEarthFilesZipFile();
		ZipFile sourceZipFile = new ZipFile(earthFilesZip);
		ZipFiles.copyFiles(sourceZipFile, zipFile, zipParameters);
		
		return outputFile;
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
			if (FIXED_CSV_ATTRIBUTES.contains(attrName)) {
				value = "${placemark." + attrName + "}";
			} else {
				value = "${placemark.extraInfo[" + extraInfoIndex + "]}";
				extraInfoIndex ++;
			}
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
			valuesSB.append(",\"value_" + attrName + "\"");
		}
		String content = templateContent.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_COLUMNS_HEADER, headerSB.toString());
		content = content.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_COLUMNS_VALUES, valuesSB.toString());
		return Files.writeToTempFile(content, "collect-earth-project-file-creator", ".ced");
	}
	
	private File generateCube(CollectSurvey survey) throws IOException {
		MondrianCubeGenerator cubeGenerator = new MondrianCubeGenerator(survey);
		String xmlSchema = cubeGenerator.generateXMLSchema();
		return Files.writeToTempFile(xmlSchema, "collect-earth-project-file-creator", ".xml");
	}

	private File getEarthFilesZipFile() throws IOException, FileNotFoundException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(EARTH_FILES_ZIP_FILE_PATH);
		File earthFilesZip = File.createTempFile("earth-files", ".zip");
		FileOutputStream fos = new FileOutputStream(earthFilesZip);
		IOUtils.copy(is, fos);
		return earthFilesZip;
	}

}