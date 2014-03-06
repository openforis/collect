package org.openforis.collect.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.data.RecordFileExportTask;
import org.openforis.collect.io.data.XMLDataExportTask;
import org.openforis.collect.io.metadata.IdmlExportTask;
import org.openforis.collect.io.metadata.SurveyBackupInfoCreatorTask;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportTask;
import org.openforis.collect.io.metadata.species.SpeciesBackupExportTask;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.WorkerStatusChangeEvent;
import org.openforis.concurrency.WorkerStatusChangeListener;
import org.openforis.idm.metamodel.EntityDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SurveyBackupJob extends Job {

	public static final String ZIP_FOLDER_SEPARATOR = "/";
	public static final String SURVEY_XML_ENTRY_NAME = "idml.xml";
	public static final String SAMPLING_DESIGN_ENTRY_NAME = "sampling_design" + ZIP_FOLDER_SEPARATOR + "sampling_design.csv";
	public static final String SPECIES_FOLDER = "species";
	public static final String SPECIES_ENTRY_FORMAT = SPECIES_FOLDER + ZIP_FOLDER_SEPARATOR + "%s.csv";
	public static final String INFO_FILE_NAME = "info.properties";
	public static final String DATA_FOLDER = "data";
	public static final String UPLOADED_FILES_FOLDER = "upload";
	
	//input
	private transient CollectSurvey survey;
	private boolean includeData;
	private boolean includeRecordFiles;
	
	//output
	private transient File outputFile;
	
	//temporary instance variable
	private transient ZipOutputStream zipOutputStream;
	
	@Autowired
	private transient SpeciesManager speciesManager;
	
	@Override
	public void init() {
		try {
			outputFile = File.createTempFile("collect", "survey_export.zip");
			zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
		addInfoPropertiesCreatorTask();
		addIdmlExportTask();
		addSamplingDesignExportTask();
		addSpeciesExportTask();
		if ( includeData && ! survey.isWork() ) {
			addDataExportTask();
			if ( includeRecordFiles ) {
				addRecordFilesExportTask();
			}
		}
		super.init();
	}
	
	@Override
	protected void execute() throws Throwable {
		super.execute();
		IOUtils.closeQuietly(zipOutputStream);
	}
	
	private void addInfoPropertiesCreatorTask() {
		SurveyBackupInfoCreatorTask task = createTask(SurveyBackupInfoCreatorTask.class);
		task.setOutputStream(zipOutputStream);
		task.addStatusChangeListener(new EntryCreatorTaskStatusChangeListener(INFO_FILE_NAME));
		addTask(task);
	}

	private void addIdmlExportTask() {
		IdmlExportTask task = createTask(IdmlExportTask.class);
		task.setSurvey(survey);
		task.setOutputStream(zipOutputStream);
		task.addStatusChangeListener(new EntryCreatorTaskStatusChangeListener(SURVEY_XML_ENTRY_NAME));
		addTask(task);
	}
	
	private void addSamplingDesignExportTask() {
		SamplingDesignExportTask task = createTask(SamplingDesignExportTask.class);
		task.setSurvey(survey);
		task.setOutputStream(zipOutputStream);
		task.addStatusChangeListener(new EntryCreatorTaskStatusChangeListener(SAMPLING_DESIGN_ENTRY_NAME));
		addTask(task);
	}

	private void addSpeciesExportTask() {
		List<CollectTaxonomy> taxonomies;
		if (survey.isWork()) {
			taxonomies = speciesManager.loadTaxonomiesBySurveyWork(survey.getId());
		} else {
			taxonomies = speciesManager.loadTaxonomiesBySurvey(survey.getId());
		}
		for (CollectTaxonomy taxonomy : taxonomies) {
			SpeciesBackupExportTask task = createTask(SpeciesBackupExportTask.class);
			task.setOutputStream(zipOutputStream);
			task.setTaxonomyId(taxonomy.getId());
			String entryName = String.format(SPECIES_ENTRY_FORMAT, taxonomy.getName());
			task.addStatusChangeListener(new EntryCreatorTaskStatusChangeListener(entryName));
			addTask(task);
		}
	}
	
	private void addDataExportTask() {
		for (EntityDefinition rootEntity : survey.getSchema().getRootEntityDefinitions()) {
			XMLDataExportTask task = createTask(XMLDataExportTask.class);
			task.setZipOutputStream(zipOutputStream);
			task.setSurvey(survey);
			task.setRootEntityName(rootEntity.getName());
			task.setZipEntryPrefix(DATA_FOLDER + ZIP_FOLDER_SEPARATOR + rootEntity.getName() + ZIP_FOLDER_SEPARATOR);
			addTask(task);
		}
	}
	
	private void addRecordFilesExportTask() {
		for (EntityDefinition rootEntity : survey.getSchema().getRootEntityDefinitions()) {
			RecordFileExportTask task = createTask(RecordFileExportTask.class);
			task.setZipOutputStream(zipOutputStream);
			task.setSurvey(survey);
			task.setRootEntityName(rootEntity.getName());
			task.setZipEntryPrefix(UPLOADED_FILES_FOLDER + ZIP_FOLDER_SEPARATOR + rootEntity.getName() + ZIP_FOLDER_SEPARATOR);
			addTask(task);
		}
	}

	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	public boolean isIncludeData() {
		return includeData;
	}

	public void setIncludeData(boolean includeData) {
		this.includeData = includeData;
	}
	
	public boolean isIncludeRecordFiles() {
		return includeRecordFiles;
	}
	
	public void setIncludeRecordFiles(boolean includeRecordFiles) {
		this.includeRecordFiles = includeRecordFiles;
	}
	
	private class EntryCreatorTaskStatusChangeListener implements WorkerStatusChangeListener {
		
		private String entryName;

		public EntryCreatorTaskStatusChangeListener(String entryName) {
			this.entryName = entryName;
		}
		
		@Override
		public void statusChanged(WorkerStatusChangeEvent event) {
			try {
				switch ( event.getTo() ) {
				case RUNNING:
					zipOutputStream.putNextEntry(new ZipEntry(entryName));
					break;
				case COMPLETED:
					zipOutputStream.closeEntry();
					break;
				default:
					break;
				}
			} catch ( IOException e ) {
				throw new RuntimeException("Error creating or closing the zip entry: " + e.getMessage(), e);
			}
		}

	}

}
