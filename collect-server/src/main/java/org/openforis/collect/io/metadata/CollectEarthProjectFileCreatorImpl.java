package org.openforis.collect.io.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CollectEarthProjectFileCreatorImpl implements CollectEarthProjectFileCreator{

	private static final String PLACEMARK_FILE_NAME = "placemark.idml.xml";
	private static final String BALOON_FILE_NAME = "baloon.html";
	private static final String CUBE_FILE_NAME = "collect_cube.xml.fmt";
	private static final String PROJECT_PROPERTIES_FILE_NAME = "project_definition.properties";
	
	private CEComponent rootComponent;

	@Override
	public File create(CollectSurvey survey) throws Exception {
		// create placemark
		File placemarkFile = createPlacemark(survey);
		
		rootComponent = generateRootComponent(survey);
		
		File baloon = generateBaloon(survey);
		
		File cube = generateCube(survey);
		
		File projectProperties = generateProjectProperties(survey);
		
		// create output zip file
		File outputFile = File.createTempFile("openforis-collect-earth-temp", ".zip");
		outputFile.delete(); //
		
		ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(outputFile);
		
		addFileToZip(zipFile, placemarkFile, PLACEMARK_FILE_NAME);
		addFileToZip(zipFile, baloon, BALOON_FILE_NAME);
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
		properties.store(writer, "");
		return null;
	}

	private CEComponent generateRootComponent(CollectSurvey survey) {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDef = schema.getRootEntityDefinitions().get(0);
		CEComponent rootComponent = createComponent(rootEntityDef);
		return rootComponent;
	}
	
	private CEComponent createComponent(NodeDefinition def) {
		String label = def.getLabel(Type.INSTANCE);
		boolean multiple = def.isMultiple();
		if (def instanceof EntityDefinition) {
			CEEntity ceEntity = new CEEntity(def.getName(), label, def.isMultiple());
			List<NodeDefinition> childDefinitions = ((EntityDefinition) def).getChildDefinitions();
			for (NodeDefinition child : childDefinitions) {
				ceEntity.addChild(createComponent(child));
			}
			return ceEntity;
		} else {
			String type = "boolean";
			boolean key = def instanceof KeyAttributeDefinition ? ((KeyAttributeDefinition) def).isKey(): false;
			if (def instanceof CodeAttributeDefinition) {
				CodeListService codeListService = def.getSurvey().getContext().getCodeListService();
				List<CodeListItem> codes = codeListService.loadRootItems(((CodeAttributeDefinition) def).getList());
				CodeAttributeDefinition parentCodeAttributeDef = ((CodeAttributeDefinition) def).getParentCodeAttributeDefinition();
				String parentName = parentCodeAttributeDef == null ? null: parentCodeAttributeDef.getName();
				return new CECodeField(def.getName(), label, type, multiple, key, codes, parentName);
			} else {
				return new CEField(def.getName(), label, multiple, type, key);
			}
		}
	}

	private File generateBaloon(CollectSurvey survey) {
		return null;
	}
	
	private File generateCube(CollectSurvey survey) throws IOException {
		MondrianCubeGenerator cubeGenerator = new MondrianCubeGenerator(survey);
		org.openforis.collect.io.metadata.MondrianCubeGenerator.Schema mondrianSchema = cubeGenerator.generateSchema();
		XStream xStream = new XStream();
		xStream.processAnnotations(MondrianCubeGenerator.Schema.class);
		String xmlSchema = xStream.toXML(mondrianSchema);
		File file = File.createTempFile("mondrian_schema", ".xml.fmt");
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(xmlSchema);
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

	private static class CEComponent {

		private String name;
		private String label;
		private boolean multiple;
		
		public CEComponent(String name, String label, boolean multiple) {
			super();
			this.name = name;
			this.label = label;
			this.multiple = multiple;
		}

		public String getName() {
			return name;
		}

		public String getLabel() {
			return label;
		}

		public boolean isMultiple() {
			return multiple;
		}
	}
	
	private static class CEEntity extends CEComponent {

		private List<CEComponent> children = new ArrayList<CEComponent>();
		
		public CEEntity(String name, String label, boolean multiple) {
			super(name, label, multiple);
		}
		
		public void addChild(CEComponent child) {
			children.add(child);
		}
		
		public List<CEComponent> getChildren() {
			return children;
		}
		
	}
	
	private static class CEField extends CEComponent {
		
		private String type;
		private boolean key;
		
		public CEField(String name, String label, boolean multiple, String type, boolean key) {
			super(name, label, multiple);
			this.type = type;
			this.key = key;
		}

		public String getType() {
			return type;
		}

		public boolean isKey() {
			return key;
		}
		
	}
	
	private static class CECodeField extends CEField {
		
		private CodeList listName;
		private List<CodeListItem> codes;
		private String parentName;

		public CECodeField(String name, String label, String type, boolean multiple, boolean key, List<CodeListItem> codes, String parentName) {
			super(name, label, multiple, type, key);
			this.codes = codes;
			this.parentName = parentName;
		}
		
		public List<CodeListItem> getCodes() {
			return codes;
		}
		
		public String getParentName() {
			return parentName;
		}
		
	}
	
	
	
}