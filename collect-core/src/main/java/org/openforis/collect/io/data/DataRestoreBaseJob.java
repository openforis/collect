/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.File;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.SurveyRestoreJob;
import org.openforis.collect.io.data.RecordProviderInitializerTask.Input;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.commons.collection.Predicate;
import org.openforis.commons.versioning.Version;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public abstract class DataRestoreBaseJob extends Job {

	private static final Version DATA_SUMMARY_FILE_COLLECT_VERSION = new Version("3.11.5");
	@Autowired
	protected SurveyManager surveyManager;
	@Autowired
	protected RecordManager recordManager;
	@Autowired
	protected RecordFileManager recordFileManager;
	@Autowired
	protected UserManager userManager;
	
	//input
	protected transient File file;
	protected transient CollectSurvey publishedSurvey; //optional: if not specified, the packaged survey will be published as a new one
	protected transient CollectSurvey packagedSurvey; //optional: if not specified, it will be extracted from the ZIP file
	protected transient User user;
	protected transient UserGroup newSurveyUserGroup;
	protected transient boolean validateRecords;
	protected transient boolean closeRecordProviderOnComplete = true;
	protected transient Predicate<CollectRecord> includeRecordPredicate;

	protected RecordProvider recordProvider; //if null it will be created and initialized, otherwise it will be re-used

	//temporary instance variables
	protected transient boolean newSurvey;
	protected transient String surveyName; //published survey name or packaged survey name
	protected transient BackupFileExtractor backupFileExtractor;
	protected transient boolean oldBackupFormat;
	protected transient File dataSummaryFile;

	@Override
	public void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		newSurvey = publishedSurvey == null;
		backupFileExtractor = new BackupFileExtractor(file);
		oldBackupFormat = backupFileExtractor.isOldFormat();
		dataSummaryFile = extractDataSummaryFile();
		surveyName = newSurvey ? extractSurveyName() : publishedSurvey.getName();
	}

	private File extractDataSummaryFile() {
		if (oldBackupFormat) {
			return null;
		} else {
			SurveyBackupInfo info = backupFileExtractor.extractInfo();
			if (info.getCollectVersion().compareTo(DATA_SUMMARY_FILE_COLLECT_VERSION) >= 0) {
				return backupFileExtractor.extractDataSummaryFile();
			} else {
				return null;
			}
		}
	}
	
	@Override
	protected void validateInput() throws Throwable {
		super.validateInput();
		BackupFileExtractor backupFileExtractor = null;
		try {
			backupFileExtractor = new BackupFileExtractor(file);
			if (backupFileExtractor.isOldFormat()) {
				if (publishedSurvey == null) {
					throw new IllegalArgumentException("Please specify a published survey to witch restore data into");
				}
			} else {
				SurveyBackupInfo backupInfo = backupFileExtractor.extractInfo();
				CollectSurvey existingPublishedSurvey = findExistingPublishedSurvey(backupInfo);
				boolean newSurvey = publishedSurvey == null;
				if (newSurvey) {
					if (existingPublishedSurvey != null) {
						throw new IllegalArgumentException(String.format("The backup file is associated to an already published survey: %s", existingPublishedSurvey.getName()));
					}
				} else {
					String publishedSurveyUri = publishedSurvey.getUri();
					String packagedSurveyUri = backupInfo.getSurveyUri();
					if (! publishedSurveyUri.equals(packagedSurveyUri)) {
						throw new RuntimeException(String.format("Packaged survey uri (%s) is different from the expected one (%s)", packagedSurveyUri, publishedSurveyUri));
					}
				}
			}
		} finally {
			IOUtils.closeQuietly(backupFileExtractor);
		}
	}

	@Override
	protected void buildTasks() throws Throwable {
		if (newSurvey) {
			addTask(SurveyRestoreJob.class);
		} else if (packagedSurvey == null) {
			addTask(createTask(IdmlUnmarshallTask.class));
		}
		if (recordProvider == null && isRecordProviderToBeInitialized()) {
			addTask(RecordProviderInitializerTask.class);
		}
	}

	@Override
	protected void initializeTask(Worker task) {
		if (task instanceof SurveyRestoreJob) {
			SurveyRestoreJob t = (SurveyRestoreJob) task;
			t.setFile(file);
			t.setRestoreIntoPublishedSurvey(true);
			t.setSurveyName(surveyName);
			t.setValidateSurvey(true);
			t.setUserGroup(newSurveyUserGroup);
		} else if ( task instanceof IdmlUnmarshallTask ) {
			IdmlUnmarshallTask t = (IdmlUnmarshallTask) task;
			File idmlFile = backupFileExtractor.extractIdmlFile();
			t.setSurveyManager(surveyManager);
			t.setFile(idmlFile);
			t.setValidate(false);
		} else if (task instanceof RecordProviderInitializerTask) {
			RecordProviderInitializerTask t = (RecordProviderInitializerTask) task;
			Input input = new Input();
			input.setFile(file);
			input.setExistingSurvey(publishedSurvey);
			input.setPackagedSurvey(packagedSurvey);
			input.setUserManager(userManager);
			input.setValidateRecords(validateRecords);
			t.setInput(input);
		}
		super.initializeTask(task);
	}
	
	@Override
	protected void onTaskCompleted(Worker task) {
		super.onTaskCompleted(task);
		if (task instanceof SurveyRestoreJob) {
			publishedSurvey = ((SurveyRestoreJob) task).getSurvey();
			packagedSurvey = publishedSurvey;
		} else if ( task instanceof IdmlUnmarshallTask ) {
			CollectSurvey survey = ((IdmlUnmarshallTask) task).getSurvey();
			if ( survey == null ) {
				throw new RuntimeException("Error extracting packaged survey");
			} else {
				packagedSurvey = survey;
			}
			this.packagedSurvey = survey;
		} else if (task instanceof RecordProviderInitializerTask) {
			this.recordProvider = ((RecordProviderInitializerTask) task).getOutput();
		}
	}
	
//	@Override
//	protected void onEnd() {
//		super.onEnd();
//		if (! this.isCompleted() || closeRecordProviderOnComplete) {
//			IOUtils.closeQuietly(recordProvider);
//		}
//	}

	private CollectSurvey findExistingPublishedSurvey(SurveyBackupInfo backupInfo) {
		CollectSurvey existingPublishedSurvey = surveyManager.get(backupInfo.getSurveyName());
		if (existingPublishedSurvey == null) {
			existingPublishedSurvey = surveyManager.getByUri(backupInfo.getSurveyUri());
		}
		return existingPublishedSurvey;
	}

	private String extractSurveyName() {
		SurveyBackupInfo info = backupFileExtractor.extractInfo();
		return info.getSurveyName();
	}

	protected boolean isRecordProviderToBeInitialized() {
		return true;
	}

	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
	public void setRecordFileManager(RecordFileManager recordFileManager) {
		this.recordFileManager = recordFileManager;
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Predicate<CollectRecord> getIncludeRecordPredicate() {
		return includeRecordPredicate;
	}
	
	public void setIncludeRecordPredicate(
			Predicate<CollectRecord> includeRecordPredicate) {
		this.includeRecordPredicate = includeRecordPredicate;
	}
	
	public void setRecordProvider(RecordProvider recordProvider) {
		this.recordProvider = recordProvider;
	}
	
	public RecordProvider getRecordProvider() {
		return recordProvider;
	}
	
	public CollectSurvey getPublishedSurvey() {
		return publishedSurvey;
	}
	
	public void setPublishedSurvey(CollectSurvey publishedSurvey) {
		this.publishedSurvey = publishedSurvey;
	}
	
	public CollectSurvey getPackagedSurvey() {
		return packagedSurvey;
	}
	
	public void setPackagedSurvey(CollectSurvey packagedSurvey) {
		this.packagedSurvey = packagedSurvey;
	}
	
	public void setValidateRecords(boolean validateRecords) {
		this.validateRecords = validateRecords;
	}
	
	public void setCloseRecordProviderOnComplete(boolean closeRecordProviderOnComplete) {
		this.closeRecordProviderOnComplete = closeRecordProviderOnComplete;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public UserGroup getNewSurveyUserGroup() {
		return newSurveyUserGroup;
	}
	
	public void setNewSurveyUserGroup(UserGroup newSurveyUserGroup) {
		this.newSurveyUserGroup = newSurveyUserGroup;
	}
}
