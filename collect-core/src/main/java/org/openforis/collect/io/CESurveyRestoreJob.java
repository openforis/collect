package org.openforis.collect.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.metadata.IdmlImportTask;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.Worker;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CESurveyRestoreJob extends AbstractSurveyRestoreJob {

	//temporary instance variables
	private transient ZipFile zipFile;
	private transient ZipFileExtractor zipFileExtractor;
	private transient File idmlFile;

	@Override
	public void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		this.zipFile = new ZipFile(file);
		this.zipFileExtractor = new ZipFileExtractor(zipFile);
		this.idmlFile = zipFileExtractor.extract("placemark.idm.xml");
	}

	@Override
	protected void buildTasks() throws Throwable {
		if ( surveyUri == null ) {
			//unmarshall xml file to get survey uri
			addTask(IdmlUnmarshallTask.class);
		}
		addTask(IdmlImportTask.class);
		addTask(new CEPropertiesImportTask());
		addTask(new GoogleEarthEnginePlaygroundScriptImportTask());
		addTask(new AreaPerAttributeImportTask());
		addTask(new GridFilesImportTask());
	}

	@Override
	protected void initializeTask(Worker task) {
		if ( task instanceof IdmlUnmarshallTask ) {
			IdmlUnmarshallTask t = (IdmlUnmarshallTask) task;
			t.setFile(idmlFile);
			t.setSurveyManager(surveyManager);
			t.setValidate(false);
		} else if ( task instanceof IdmlImportTask ) {
			IdmlImportTask t = (IdmlImportTask) task;
			t.setSurveyManager(surveyManager);
			t.setFile(idmlFile);
			t.setSurveyUri(surveyUri);
			t.setSurveyName(surveyName);
			t.setImportInPublishedSurvey(restoreIntoPublishedSurvey);
			t.setActiveUser(activeUser);
			t.setUserGroup(userGroup);
			t.setValidate(false);
		} else if ( task instanceof CEPropertiesImportTask ) {
			CEPropertiesImportTask t = (CEPropertiesImportTask) task;
			t.setSurveyManager(surveyManager);
			t.setSurvey(survey);
			t.setFile(zipFileExtractor.extract("project_definition.properties"));
		} else if ( task instanceof CollectEarthUniqueSurveyFileImportTask) {
			CollectEarthUniqueSurveyFileImportTask t = (CollectEarthUniqueSurveyFileImportTask) task;
			t.setSurveyManager(surveyManager);
			t.setSurvey(survey);
			t.setZipFileExtractor(zipFileExtractor);
		} else if ( task instanceof GridFilesImportTask ) {
			GridFilesImportTask t = (GridFilesImportTask) task;
			t.setSurveyManager(surveyManager);
			t.setSurvey(survey);
			t.setZipFileExtractor(zipFileExtractor);
		}
		super.initializeTask(task);
	}

	@Override
	protected void onTaskCompleted(Worker task) {
		if ( task instanceof IdmlUnmarshallTask ) {
			this.survey = ((IdmlUnmarshallTask) task).getSurvey();
			this.surveyUri = this.survey.getUri();
		} else if ( task instanceof IdmlImportTask ) {
			IdmlImportTask t = (IdmlImportTask) task;
			//get output survey and set it into job instance instance variable
			this.survey = t.getSurvey();
		}
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		FileUtils.deleteQuietly(idmlFile);
	}

	private static class CEPropertiesImportTask extends Task {

		private static final double[] PREDEFINED_PLOT_AREAS = new double[]{0.25d, 0.50d, 1.0d, 5.0d, 10.0d};
		// input
		private SurveyManager surveyManager;
		private CollectSurvey survey;
		private File file;

		@Override
		protected void execute() throws Throwable {
			Properties p = loadPropertiesFromFile();
			CollectAnnotations annotations = survey.getAnnotations();
			annotations.setBingMapsKey((String) p.get("bing_maps_key"));
			annotations.setBingMapsEnabled(Boolean.parseBoolean(p.getProperty("open_bing_maps")));
			//annotations.setPlanetMapsKey((String) p.get("planet_maps_key"));
			annotations.setPlanetMapsEnabled(Boolean.parseBoolean(p.getProperty("open_planet_maps")));

			annotations.setEarthMapEnabled(Boolean.parseBoolean(p.getProperty("open_earth_map")));
			annotations.setYandexMapsEnabled(Boolean.parseBoolean(p.getProperty("open_yandex_maps")));
			annotations.setStreetViewEnabled(Boolean.parseBoolean(p.getProperty("open_street_view")));
			annotations.setGEEExplorerEnabled(Boolean.parseBoolean(p.getProperty("open_earth_engine")));
			annotations.setGEECodeEditorEnabled(Boolean.parseBoolean(p.getProperty("open_gee_playground")));
			annotations.setGEEAppEnabled(Boolean.parseBoolean(p.getProperty("open_gee_app")));
			annotations.setSecureWatchEnabled(Boolean.parseBoolean(p.getProperty("open_maxar_securewatch")));
			annotations.setCollectEarthSamplePoints(getIntegerProperty(p, "number_of_sampling_points_in_plot", 9));
			annotations.setCollectEarthPlotArea(calculatePlotArea(p));
			surveyManager.save(survey);
		}

		private Properties loadPropertiesFromFile() throws FileNotFoundException, IOException {
			Properties p = new Properties();
			FileInputStream is = null;
			try {
				is = new FileInputStream(file);
				p.load(is);
			} finally {
				IOUtils.closeQuietly(is);
			}
			return p;
		}

		private double calculatePlotArea(Properties p) {
			int numberOfSamplingPoints = getIntegerProperty(p, "number_of_sampling_points_in_plot", 9);
			int distanceBetweenSamplePoints = getIntegerProperty(p, "distance_between_sample_points", 10);
			int distanceToPlotBoundaries = getIntegerProperty(p, "distance_to_plot_boundaries", 5);

			double plotArea = Math.pow((Math.sqrt(numberOfSamplingPoints) - 1) * distanceBetweenSamplePoints + (distanceToPlotBoundaries * 2), 2);
			double plotAreaHa = plotArea / 10000;
			double roundedPlotAreaHa = 1.0d;
			if (plotAreaHa > 0) {
				for (double predefinedValue : PREDEFINED_PLOT_AREAS) {
					if (plotAreaHa <= predefinedValue) {
						roundedPlotAreaHa = predefinedValue;
						break;
					}
				}
			}
			return roundedPlotAreaHa;
		}

		private int getIntegerProperty(Properties p, String key, int defaultValue) {
			String valueStr = p.getProperty(key);
			if (StringUtils.isBlank(valueStr)) {
				return defaultValue;
			} else {
				return Integer.parseInt(valueStr);
			}
		}

		public void setSurveyManager(SurveyManager surveyManager) {
			this.surveyManager = surveyManager;
		}

		public void setFile(File file) {
			this.file = file;

		}
		public void setSurvey(CollectSurvey survey) {
			this.survey = survey;
		}
	}

	private static abstract class CollectEarthUniqueSurveyFileImportTask extends Task {

		// input
		private SurveyFileType surveyFileType;
		private SurveyManager surveyManager;
		private CollectSurvey survey;
		private ZipFileExtractor zipFileExtractor;

		public CollectEarthUniqueSurveyFileImportTask(SurveyFileType surveyFileType) {
			super();
			this.surveyFileType = surveyFileType;
		}

		@Override
		protected void execute() throws Throwable {
			String filename = surveyFileType.getFixedFilename();
			File tempFile = zipFileExtractor.extract(filename, false);
			if (tempFile != null) {
				//delete old survey file
				List<SurveyFile> surveyFileSummaries = surveyManager.loadSurveyFileSummaries(survey);
				for (SurveyFile surveyFile : surveyFileSummaries) {
					if (surveyFile.getType() == surveyFileType) {
						surveyManager.deleteSurveyFile(surveyFile);
					}
				}
				SurveyFile surveyFile = new SurveyFile(survey);
				surveyFile.setFilename(filename);
				surveyFile.setType(surveyFileType);
				surveyManager.addSurveyFile(survey, surveyFile, tempFile);
				FileUtils.deleteQuietly(tempFile);
			}
		}

		public void setSurveyManager(SurveyManager surveyManager) {
			this.surveyManager = surveyManager;
		}

		public void setSurvey(CollectSurvey survey) {
			this.survey = survey;
		}

		public void setZipFileExtractor(ZipFileExtractor zipFileExtractor) {
			this.zipFileExtractor = zipFileExtractor;
		}
	}

	private static class GoogleEarthEnginePlaygroundScriptImportTask extends CollectEarthUniqueSurveyFileImportTask {

		public GoogleEarthEnginePlaygroundScriptImportTask() {
			super(SurveyFileType.COLLECT_EARTH_EE_SCRIPT);
		}
	}

	private static class AreaPerAttributeImportTask extends CollectEarthUniqueSurveyFileImportTask {

		public AreaPerAttributeImportTask() {
			super(SurveyFileType.COLLECT_EARTH_AREA_PER_ATTRIBUTE);
		}
	}

	private static class GridFilesImportTask extends Task {

		// input
		private SurveyManager surveyManager;
		private CollectSurvey survey;
		private ZipFileExtractor zipFileExtractor;

		@Override
		protected long countTotalItems() {
			String gridFilesPath = determineGridFilesPath();
			if (gridFilesPath == null) {
				return 0;
			} else {
				return zipFileExtractor.countEntriesInPath(determineGridFilesPath());
			}
		}

		@Override
		protected void execute() throws Throwable {
			String surveyFilesPath = determineGridFilesPath();
			if (surveyFilesPath != null) {
				//delete old grid files
				List<SurveyFile> surveyFileSummaries = surveyManager.loadSurveyFileSummaries(survey);
				for (SurveyFile surveyFile : surveyFileSummaries) {
					if (surveyFile.getType() == SurveyFileType.COLLECT_EARTH_GRID) {
						surveyManager.deleteSurveyFile(surveyFile);
					}
				}
				List<String> entryNames = zipFileExtractor.listEntriesInPath(surveyFilesPath);
				for (String entryName : entryNames) {
					File tempFile = zipFileExtractor.extract(entryName);
					String fileName = FilenameUtils.getName(entryName);
					SurveyFile surveyFile = new SurveyFile(survey);
					surveyFile.setFilename(fileName);
					surveyFile.setType(SurveyFileType.COLLECT_EARTH_GRID);
					surveyManager.addSurveyFile(survey, surveyFile, tempFile);
					FileUtils.deleteQuietly(tempFile);
				}
			}
		}

		private String determineGridFilesPath() {
			String[] gridPaths = new String[]{"grid", "grids"};
			for (String path : gridPaths) {
				int count = zipFileExtractor.countEntriesInPath(path);
				if (count > 0) {
					return path;
				}
			}
			return null;
		}

		public void setSurveyManager(SurveyManager surveyManager) {
			this.surveyManager = surveyManager;
		}

		public void setSurvey(CollectSurvey survey) {
			this.survey = survey;
		}

		public void setZipFileExtractor(ZipFileExtractor backupFileExtractor) {
			this.zipFileExtractor = backupFileExtractor;
		}
	}
}
