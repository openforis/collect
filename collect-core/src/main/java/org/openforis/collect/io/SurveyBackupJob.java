package org.openforis.collect.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.datacleansing.io.DataCleansingExportTask;
import org.openforis.collect.io.data.DataBackupError;
import org.openforis.collect.io.data.DataBackupTask;
import org.openforis.collect.io.data.RecordFileBackupTask;
import org.openforis.collect.io.data.RecordFileBackupTask.MissingRecordFileError;
import org.openforis.collect.io.data.backup.BackupStorageManager;
import org.openforis.collect.io.internal.SurveyBackupInfoCreatorTask;
import org.openforis.collect.io.metadata.CodeListImagesExportTask;
import org.openforis.collect.io.metadata.CollectMobileBackupConvertTask;
import org.openforis.collect.io.metadata.IdmlExportTask;
import org.openforis.collect.io.metadata.SurveyFileExportTask;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportTask;
import org.openforis.collect.io.metadata.species.SpeciesBackupExportTask;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.collect.utils.ZipFiles;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.Worker;
import org.openforis.idm.metamodel.EntityDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SurveyBackupJob extends SurveyLockingJob {

	public static final String ZIP_FOLDER_SEPARATOR = "/";
	public static final String SURVEY_XML_ENTRY_NAME = "idml.xml";
	public static final String SAMPLING_DESIGN_ENTRY_NAME = "sampling_design" + ZIP_FOLDER_SEPARATOR + "sampling_design.csv";
	public static final String SPECIES_FOLDER = "species";
	public static final String SPECIES_ENTRY_FORMAT = SPECIES_FOLDER + ZIP_FOLDER_SEPARATOR + "%s.csv";
	public static final String CLEANSING_FOLDER_NAME = "cleansing";
	public static final String DATA_CLEANSING_METADATA_ENTRY_NAME = CLEANSING_FOLDER_NAME + ZIP_FOLDER_SEPARATOR + "cleansing_metadata.json";
	public static final String INFO_FILE_NAME = "info.properties";
	public static final String DATA_FOLDER = "data";
	public static final String DATA_SUMMARY_FILE_NAME = "summary.csv";
	public static final String DATA_SUMMARY_ENTRY_NAME = DATA_FOLDER + ZIP_FOLDER_SEPARATOR + DATA_SUMMARY_FILE_NAME;
	public static final String CODE_LIST_IMAGES_FOLDER = "code_list_images";
	public static final String UPLOADED_FILES_FOLDER = "upload";
	public static final String SURVEY_FILES_FOLDER = "files";

	public enum OutputFormat {
		DESKTOP("collect"), 
		DESKTOP_FULL("collect-backup"),
		MOBILE("collect-mobile"),
		ONLY_DATA("collect-data");
		
		public static final OutputFormat DEFAULT = DESKTOP;
		
		private String outputFileExtension;

		OutputFormat(String outputFileExtension) {
			this.outputFileExtension = outputFileExtension;
		}
		
		public String getOutputFileExtension() {
			return outputFileExtension;
		}
	}
	
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
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private BackupStorageManager backupStorageManager;
	@Autowired
	private ApplicationContext applicationContext;
	
	//input
	private boolean full;
	private boolean includeData;
	private boolean includeRecordFiles;
	private RecordFilter recordFilter;
	private String outputSurveyDefaultLanguage;
	private OutputFormat outputFormat;
	
	//output
	private File outputFile;
	private boolean outputFileCreated = false; //created or passed from outside
	private List<DataBackupError> dataBackupErrors = new ArrayList<DataBackupError>();
	
	//temporary instance variable
	private ZipOutputStream zipOutputStream;
	
	public SurveyBackupJob() {
		outputFormat = OutputFormat.DEFAULT;
		full = false;
	}
	
	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		if ( outputFile == null ) {
			createOutputFile();
		}
		zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
	}

	private void createOutputFile() throws IOException {
		String outputFileExtension;
		switch (outputFormat) {
		case MOBILE:
			//temporary file will be of type "desktop", then it will be converted into collect-mobile format
			outputFileExtension = OutputFormat.DESKTOP.getOutputFileExtension();
			break;
		default:
			outputFileExtension = outputFormat.getOutputFileExtension();
		}
		outputFile = File.createTempFile("collect_survey_export", "." + outputFileExtension);
		outputFileCreated = true;
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		addInfoPropertiesCreatorTask();
		addIdmlExportTask();
		addCodeListImagesExportTask();
		addSamplingDesignExportTask();
		addSpeciesExportTask();
		addCleansingExportTask();
		addSurveyFileExportTask();
		if ( includeData && ! survey.isTemporary() ) {
			addDataExportTask();
			if ( includeRecordFiles ) {
				addRecordFilesBackupTask();
			}
		}
		if (outputFormat == OutputFormat.MOBILE) {
			addCollectMobileBackupConverterTask();
		}
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		if (full) {
			IOUtils.closeQuietly(zipOutputStream);
			backupStorageManager.store(survey.getName(), outputFile);
		}
	}

	@Override
	protected void onEnd() {
		IOUtils.closeQuietly(zipOutputStream);
	}

	@Override
	protected void onTaskCompleted(Worker task) {
		if (task instanceof DataBackupTask) {
			this.dataBackupErrors.addAll(((DataBackupTask) task).getErrors());
		} else if (task instanceof RecordFileBackupTask) {
			List<MissingRecordFileError> errors = ((RecordFileBackupTask) task).getMissingRecordFiles();
			for (MissingRecordFileError error : errors) {
				CollectRecordSummary recordSummary = error.getRecordSummary();
				this.dataBackupErrors.add(new DataBackupError(recordSummary.getId(), recordSummary.getRootEntityKeyValues(), 
						recordSummary.getStep(), String.format("Missing file for attribute %s: %s", 
						error.getFileAttributePath(), error.getFilePath())));
			}
		} else if ( task instanceof CollectMobileBackupConvertTask ) {
			this.zipOutputStream = null;
			this.outputFile = ((CollectMobileBackupConvertTask) task).getOutputFile();
		} else if (task instanceof DataCleansingExportTask) {
			File metadataFile = ((DataCleansingExportTask) task).getResultFile();
			if (metadataFile.length() > 0) {
				ZipFiles.writeFile(zipOutputStream, metadataFile, 
						DATA_CLEANSING_METADATA_ENTRY_NAME);
			}
		}
		super.onTaskCompleted(task);
	}
	
	@Override
	public void release() {
		super.release();
		IOUtils.closeQuietly(zipOutputStream);
		if (outputFileCreated && outputFile != null && outputFile.exists()) {
			outputFile.delete();
		}
	}

	private void addInfoPropertiesCreatorTask() {
		SurveyBackupInfoCreatorTask task = createTask(SurveyBackupInfoCreatorTask.class);
		task.setOutputStream(zipOutputStream);
		task.setSurvey(survey);
		task.addStatusChangeListener(new ZipEntryCreatorTaskStatusChangeListener(zipOutputStream, INFO_FILE_NAME));
		addTask(task);
	}

	private void addIdmlExportTask() {
		IdmlExportTask task = createTask(IdmlExportTask.class);
		task.setSurvey(survey);
		task.setOutputStream(zipOutputStream);
		task.setOutputSurveyDefaultLanguage(outputSurveyDefaultLanguage);
		task.addStatusChangeListener(new ZipEntryCreatorTaskStatusChangeListener(zipOutputStream, SURVEY_XML_ENTRY_NAME));
		addTask(task);
	}
	
	private void addCodeListImagesExportTask() {
		CodeListImagesExportTask task = createTask(CodeListImagesExportTask.class);
		task.setSurvey(survey);
		task.setZipOutputStream(zipOutputStream);
		task.setCodeListManager(codeListManager);
		addTask(task);
	}
	
	private void addSamplingDesignExportTask() {
		if ( samplingDesignManager.hasSamplingDesign(survey) ) {
			SamplingDesignExportTask task = createTask(SamplingDesignExportTask.class);
			task.setSurvey(survey);
			task.setOutputStream(zipOutputStream);
			task.setCloseWriterOnEnd(false);
			task.addStatusChangeListener(new ZipEntryCreatorTaskStatusChangeListener(zipOutputStream, SAMPLING_DESIGN_ENTRY_NAME));
			addTask(task);
		}
	}

	private void addSpeciesExportTask() {
		List<CollectTaxonomy> taxonomies = speciesManager.loadTaxonomiesBySurvey(survey);
		for (CollectTaxonomy taxonomy : taxonomies) {
//			if ( speciesManager.hasTaxons(taxonomy.getId()) ) {
				SpeciesBackupExportTask task = createTask(SpeciesBackupExportTask.class);
				task.setOutputStream(zipOutputStream);
				task.setSurvey(survey);
				task.setTaxonomyId(taxonomy.getId());
				task.setCloseWriterOnEnd(false);
				String entryName = String.format(SPECIES_ENTRY_FORMAT, taxonomy.getName());
				task.addStatusChangeListener(new ZipEntryCreatorTaskStatusChangeListener(zipOutputStream, entryName));
				addTask(task);
//			}
		}
	}
	
	private void addDataExportTask() {
		DataBackupTask task = createTask(DataBackupTask.class);
		task.setWeight(5);
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
			task.setWeight(5);
			task.setRecordManager(recordManager);
			task.setRecordFileManager(recordFileManager);
			task.setZipOutputStream(zipOutputStream);
			task.setSurvey(survey);
			task.setRootEntityName(rootEntity.getName());
			addTask(task);
		}
	}
	
	private void addCollectMobileBackupConverterTask() {
		CollectMobileBackupConvertTask task = new CollectMobileBackupConvertTask();
		task.setCollectBackupFile(outputFile);
		task.setSurveyName(survey.getName());
		addTask(task);
	}
	
	private void addCleansingExportTask() {
		try {
			DataCleansingExportTask task = applicationContext.getBean(DataCleansingExportTask.class);
			task.setSurvey(survey);
			addTask((Task) task);
		} catch (BeansException e) {
			//do nothing
		}
	}
	
	private void addSurveyFileExportTask() {
		SurveyFileExportTask task = applicationContext.getBean(SurveyFileExportTask.class);
		task.setSurvey(survey);
		task.setZipOutputStream(zipOutputStream);
		addTask(task);
	}
	
	@Override
	protected void initializeTask(Worker task) {
		if ( task instanceof CollectMobileBackupConvertTask ) {
			IOUtils.closeQuietly(zipOutputStream);
		}
		super.initializeTask(task);
	}

	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public void setDataMarshaller(DataMarshaller dataMarshaller) {
		this.dataMarshaller = dataMarshaller;
	}

	public File getOutputFile() {
		return outputFile;
	}
	
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
	public OutputFormat getOutputFormat() {
		return outputFormat;
	}
	
	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
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
	
	public String getOutputSurveyDefaultLanguage() {
		return outputSurveyDefaultLanguage;
	}
	
	public void setOutputSurveyDefaultLanguage(String outputSurveyDefaultLanguage) {
		this.outputSurveyDefaultLanguage = outputSurveyDefaultLanguage;
	}
	
	public boolean isFull() {
		return full;
	}
	
	public void setFull(boolean full) {
		this.full = full;
	}

	public List<DataBackupError> getDataBackupErrors() {
		return CollectionUtils.unmodifiableList(dataBackupErrors);
	}

}
