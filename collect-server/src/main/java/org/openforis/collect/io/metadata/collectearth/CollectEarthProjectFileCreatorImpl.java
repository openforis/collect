package org.openforis.collect.io.metadata.collectearth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.metadata.collectearth.balloon.CollectEarthBalloonGenerator;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
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

	private static final String KML_TEMPLATE_TXT = "org/openforis/collect/designer/templates/collect_earth_kml_template.txt";
	private static final String PLACEMARK_FILE_NAME = "placemark.idml.xml";
	private static final String BALLOON_FILE_NAME = "balloon.html";
	private static final String KML_TEMPLATE_FILE_NAME = "kml_template.fmt";
	private static final String CUBE_FILE_NAME = "collect_cube.xml.fmt";
	private static final String PROJECT_PROPERTIES_FILE_NAME = "project_definition.properties";
	
	@Override
	public File create(CollectSurvey survey) throws Exception {
		// create placemark
		File placemarkFile = createPlacemark(survey);
		
		File projectProperties = generateProjectProperties(survey);
		File balloon = generateBalloon(survey);
		File cube = generateCube(survey);
		File kmlTemplate = generateKMLTemplate(survey);
		
		// create output zip file
		File outputFile = File.createTempFile("openforis-collect-earth-temp", ".zip");
		outputFile.delete(); //
		
		ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(outputFile);
		
		addFileToZip(zipFile, projectProperties, PROJECT_PROPERTIES_FILE_NAME);
		addFileToZip(zipFile, placemarkFile, PLACEMARK_FILE_NAME);
		addFileToZip(zipFile, balloon, BALLOON_FILE_NAME);
		addFileToZip(zipFile, cube, CUBE_FILE_NAME);
		addFileToZip(zipFile, kmlTemplate, KML_TEMPLATE_FILE_NAME);
			
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
		Properties properties = new Properties();
		properties.put("survey_name", survey.getName());
		properties.put("balloon", "${project_path}/balloon.html");
		properties.put("metadata_file", "${project_path}/placemark.idm.xml");
		properties.put("template", "${project_path}/kml_template.fmt");
		properties.put("csv", "${project_path}/test_plots.ced");
		properties.put("sample_shape", "SQUARE");
		properties.put("distance_between_sample_points", "20");
		properties.put("distance_to_plot_boundaries", "10");
		properties.put("number_of_sampling_points_in_plot", "25");
		properties.put("inner_point_side", "2");
		properties.put("open_bing_maps", "true");
		properties.put("open_earth_engine", "true");
		File file = File.createTempFile("collect-earth-project", ".properties");
		FileWriter writer = new FileWriter(file);
		properties.store(writer, null);
		return file;
	}

	private File generateBalloon(CollectSurvey survey) throws IOException {
		CollectEarthBalloonGenerator generator = new CollectEarthBalloonGenerator(survey);
		String html = generator.generateHTML();
		return writeToTempFile(html);
	}
	
	private File generateKMLTemplate(CollectSurvey survey) throws IOException {
		//copy the template txt file into a String
		InputStream is = getClass().getClassLoader().getResourceAsStream(KML_TEMPLATE_TXT);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		String templateContent = writer.toString();
		
		//find "fromCSV" attributes
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
		//write the dynamic content to be replaced into the template
		StringBuffer sb = new StringBuffer();
		int extraInfoIndex = 0;
		for (AttributeDefinition attrDef : fromCsvAttributes) {
			sb.append("<Data name=\"" + attrDef.getName() + "\">\n");
			sb.append("<value>${placemark.extraInfo[" + extraInfoIndex + "]}</value>\n");
		    sb.append("</Data>\n");
		    extraInfoIndex ++;
		}
		String content = templateContent.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_CSV_DATA, sb.toString());
		return writeToTempFile(content);
	}
	
	private File generateCube(CollectSurvey survey) throws IOException {
		MondrianCubeGenerator cubeGenerator = new MondrianCubeGenerator(survey);
		String xmlSchema = cubeGenerator.generateXMLSchema();
		return writeToTempFile(xmlSchema);
	}

	private File writeToTempFile(String text) throws IOException {
		File file = File.createTempFile("collect-earth-project-file-creator", ".xml");
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(text);
		} finally {
			IOUtils.closeQuietly(writer);
		}
		return file;
	}

	private void addFileToZip(ZipFile zipFile, File placemarkFile, String fileName) throws ZipException {
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setSourceExternalStream(true);
		zipParameters.setFileNameInZip(fileName);
		zipFile.addFile(placemarkFile, zipParameters);
	}

}