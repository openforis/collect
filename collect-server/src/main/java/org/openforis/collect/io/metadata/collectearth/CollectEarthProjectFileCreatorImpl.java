package org.openforis.collect.io.metadata.collectearth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CollectEarthProjectFileCreatorImpl implements CollectEarthProjectFileCreator{

	private static final String PLACEMARK_FILE_NAME = "placemark.idml.xml";
	private static final String BALLOON_FILE_NAME = "balloon.html";
	private static final String CUBE_FILE_NAME = "collect_cube.xml.fmt";
	private static final String PROJECT_PROPERTIES_FILE_NAME = "project_definition.properties";
	
	@Override
	public File create(CollectSurvey survey) throws Exception {
		// create placemark
		File placemarkFile = createPlacemark(survey);
		
		File projectProperties = generateProjectProperties(survey);
		File cube = generateCube(survey);
		File balloon = generateBalloon(survey);
		
		// create output zip file
		File outputFile = File.createTempFile("openforis-collect-earth-temp", ".zip");
		outputFile.delete(); //
		
		ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(outputFile);
		
		addFileToZip(zipFile, placemarkFile, PLACEMARK_FILE_NAME);
		addFileToZip(zipFile, balloon, BALLOON_FILE_NAME);
		addFileToZip(zipFile, cube, CUBE_FILE_NAME);
		addFileToZip(zipFile, projectProperties, PROJECT_PROPERTIES_FILE_NAME);
			
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