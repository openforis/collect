package org.openforis.collect.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.data.DataBackupTask;
import org.openforis.collect.io.data.RecordFileBackupTask;
import org.openforis.collect.io.internal.SurveyBackupInfoCreatorTask;
import org.openforis.collect.io.metadata.IdmlExportTask;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportTask;
import org.openforis.collect.io.metadata.species.SpeciesBackupExportTask;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.xml.DataMarshaller;
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
	
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private RecordFileManager recordFileManager;
	@Autowired
	private DataMarshaller dataMarshaller;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	//input
	private CollectSurvey survey;
	private boolean includeData;
	private boolean includeRecordFiles;
	private RecordFilter recordFilter;
	
	//output
	private File outputFile;
	
	//temporary instance variable
	private ZipOutputStream zipOutputStream;
	
	@Override
	protected void buildTasks() throws Throwable {
		addInfoPropertiesCreatorTask();
		addIdmlExportTask();
		addSamplingDesignExportTask();
		addSpeciesExportTask();
		if ( includeData && ! survey.isWork() ) {
			addDataExportTask();
			if ( includeRecordFiles ) {
				addRecordFilesBackupTask();
			}
		}
	}
	
	@Override
	protected void initInternal() throws Throwable {
		if ( outputFile == null ) {
			outputFile = File.createTempFile("collect", "survey_export.zip");
		}
		zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
		super.initInternal();
	}
	
	@Override
	protected void onEnd() {
		IOUtils.closeQuietly(zipOutputStream);
	}
	
	private void addInfoPropertiesCreatorTask() {
		SurveyBackupInfoCreatorTask task = createTask(SurveyBackupInfoCreatorTask.class);
		task.setOutputStream(zipOutputStream);
		task.setSurvey(survey);
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
		if ( samplingDesignManager.hasSamplingDesign(survey) ) {
			SamplingDesignExportTask task = createTask(SamplingDesignExportTask.class);
			task.setSamplingDesignManager(samplingDesignManager);
			task.setSurvey(survey);
			task.setOutputStream(zipOutputStream);
			task.addStatusChangeListener(new EntryCreatorTaskStatusChangeListener(SAMPLING_DESIGN_ENTRY_NAME));
			addTask(task);
		}
	}

	private void addSpeciesExportTask() {
		List<CollectTaxonomy> taxonomies;
		if (survey.isWork()) {
			taxonomies = speciesManager.loadTaxonomiesBySurveyWork(survey.getId());
		} else {
			taxonomies = speciesManager.loadTaxonomiesBySurvey(survey.getId());
		}
		for (CollectTaxonomy taxonomy : taxonomies) {
			if ( speciesManager.hasTaxons(taxonomy.getId()) ) {
				SpeciesBackupExportTask task = createTask(SpeciesBackupExportTask.class);
				task.setSpeciesManager(speciesManager);
				task.setOutputStream(zipOutputStream);
				task.setTaxonomyId(taxonomy.getId());
				String entryName = String.format(SPECIES_ENTRY_FORMAT, taxonomy.getName());
				task.addStatusChangeListener(new EntryCreatorTaskStatusChangeListener(entryName));
				addTask(task);
			}
		}
	}
	
	private void addDataExportTask() {
		DataBackupTask task = createTask(DataBackupTask.class);
		task.setRecordManager(recordManager);
		task.setDataMarshaller(dataMarshaller);
		task.setRecordFilter(recordFilter);
		task.setZipOutputStream(zipOutputStream);
		task.setSurvey(survey);
		addTask(task);
	}
	
	private void addRecordFilesBackupTask() {
		for (EntityDefinition rootEntity : survey.getSchema().getRootEntityDefinitions()) {
			RecordFileBackupTask task = createTask(RecordFileBackupTask.class);
			task.setRecordManager(recordManager);
			task.setRecordFileManager(recordFileManager);
			task.setZipOutputStream(zipOutputStream);
			task.setSurvey(survey);
			task.setRootEntityName(rootEntity.getName());
			addTask(task);
		}
	}
	
	public RecordManager getRecordManager() {
		return recordManager;
	}
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public DataMarshaller getDataMarshaller() {
		return dataMarshaller;
	}
	
	public void setDataMarshaller(DataMarshaller dataMarshaller) {
		this.dataMarshaller = dataMarshaller;
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
	
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
	public boolean isIncludeData() {
		return includeData;
	}

	public void setIncludeData(boolean includeData) {
		this.includeData = includeData;
	}
	
	public RecordFilter getRecordFilter() {
		return recordFilter;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
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
