package org.openforis.collect.io.metadata.collectearth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
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

	private static final String DEFAULT_EARTH_FILES_ZIP_URL = "http://www.openforis.org/collect-earth-files/1.0/earth-files-1_0.zip";
	private static final String KML_TEMPLATE_TXT = "org/openforis/collect/designer/templates/collectearth/kml_template.txt";
	private static final String PLACEMARK_FILE_NAME = "placemark.idm.xml";
	private static final String BALLOON_FILE_NAME = "balloon.html";
	private static final String KML_TEMPLATE_FILE_NAME = "kml_template.fmt";
	private static final String CUBE_FILE_NAME = "collectEarthCubes.xml.fmt";
	private static final String PROJECT_PROPERTIES_FILE_NAME = "project_definition.properties";
	
	private static final Set<String> FIXED_CSV_ATTRIBUTES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"elevation", "aspect", "slope")));
	
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
		outputFile.delete(); //prevent exception creating zip file with zip4j
		
		ZipFile zipFile = new ZipFile(outputFile);
		
		ZipFiles.addFile(zipFile, projectProperties, PROJECT_PROPERTIES_FILE_NAME);
		ZipFiles.addFile(zipFile, placemarkFile, PLACEMARK_FILE_NAME);
		ZipFiles.addFile(zipFile, balloon, BALLOON_FILE_NAME);
		ZipFiles.addFile(zipFile, cube, CUBE_FILE_NAME);
		ZipFiles.addFile(zipFile, kmlTemplate, KML_TEMPLATE_FILE_NAME);
		
		// include earthFiles assets folder (js, css, etc.)
		File earthFilesZip = RemoteFiles.download(DEFAULT_EARTH_FILES_ZIP_URL);
		ZipFile sourceZipFile = new ZipFile(earthFilesZip);
		ZipFiles.copyFiles(sourceZipFile, zipFile);
		
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
		File file = File.createTempFile("collect-earth-project", ".properties");
		FileWriter writer = new FileWriter(file);
		p.store(writer, null);
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
			String attrName = attrDef.getName();
			sb.append("<Data name=\"" + attrName + "\">\n");
			String value;
			if (FIXED_CSV_ATTRIBUTES.contains(attrName)) {
				value = "${placemark." + attrName + "}";
			} else {
				value = "${placemark.extraInfo[" + extraInfoIndex + "]}";
			}
			sb.append("<value>");
			sb.append(value);
			sb.append("</value>\n");
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

	private static class RemoteFiles {
		
		public static File download(String url) throws IOException {
			File tempFile = File.createTempFile("collect-temp-file", ".zip");
			return download(url, tempFile);
		}

		private static File download(String fileAddress, File destFile) throws IOException {
			FileOutputStream fos = null;
			try {
				URL url = new URL(fileAddress);
				HttpURLConnection urlconn = (HttpURLConnection) url.openConnection();
		        urlconn.setConnectTimeout(100000);
		        urlconn.setReadTimeout(10000);
		        urlconn.setRequestMethod("GET");
		        urlconn.connect();
		        fos = new FileOutputStream(destFile);
		        IOUtils.copy(urlconn.getInputStream(), fos);
				return destFile;
			} finally {
				IOUtils.closeQuietly(fos);
			}
		}
	}

	private static class ZipFiles {
		
		@SuppressWarnings("unchecked")
		public static void copyFiles(ZipFile sourceFile, ZipFile destFile) throws ZipException, IOException {
			for (FileHeader header : (List<FileHeader>) sourceFile.getFileHeaders()) {
				if (! header.isDirectory()) {
					ZipInputStream is = sourceFile.getInputStream(header);
					File tempFile = File.createTempFile("temp_folder", "");
					IOUtils.copy(is, new FileOutputStream(tempFile));
					addFile(destFile, tempFile, header.getFileName());
				}
			}
		}
		
		public static void addFile(ZipFile destFile, File file, String fileNameInZip) throws ZipException {
			ZipParameters zipParameters = new ZipParameters();
			zipParameters.setSourceExternalStream(true);
			zipParameters.setFileNameInZip(fileNameInZip);
			destFile.addFile(file, zipParameters);
		}
	}
	

}