package org.openforis.collect.io.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.NewBackupFileExtractor;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.UserPersistenceException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.concurrency.ProgressListener;

public class XMLParsingRecordProvider implements RecordProvider {

	private final File file;
	private final CollectSurvey packagedSurvey;
	private final CollectSurvey existingSurvey;
	private boolean initializeRecords;
	private boolean validateRecords;
	private boolean ignoreDuplicateRecordKeyValidationErrors;
	private RecordProviderConfiguration config = new RecordProviderConfiguration();
	
	//internal
	private NewBackupFileExtractor backupFileExtractor;
	private DataUnmarshaller dataUnmarshaller;
	private RecordUpdater recordUpdater;
	private RecordUserLoader recordUserLoader;
	private User activeUser;
	private UserManager userManager;
	
	
	public XMLParsingRecordProvider(File file, CollectSurvey packagedSurvey, CollectSurvey existingSurvey, 
			User activeUser, UserManager userManager, boolean initializeRecords, boolean validateRecords, 
			boolean ignoreDuplicateRecordKeyValidationErrors) {
		this.file = file;
		this.packagedSurvey = packagedSurvey;
		this.existingSurvey = existingSurvey;
		this.activeUser = activeUser;
		this.userManager = userManager;
		this.initializeRecords = initializeRecords;
		this.validateRecords = validateRecords;
		this.ignoreDuplicateRecordKeyValidationErrors = ignoreDuplicateRecordKeyValidationErrors;
	}
	
	@Override
	public void init() throws Exception {
		init(null);
	}
	
	@Override
	public void init(ProgressListener progressListener) throws Exception {
		this.backupFileExtractor = new NewBackupFileExtractor(file);
		this.backupFileExtractor.init(progressListener);
		this.dataUnmarshaller = new DataUnmarshaller(existingSurvey == null ? packagedSurvey : existingSurvey, packagedSurvey);
		initDataUnmarshaller();
		initRecordUpdater();
		initializeRecordUserLoader();
	}

	private void initDataUnmarshaller() {
		this.dataUnmarshaller.setRecordValidationEnabled(initializeRecords && validateRecords);
		this.dataUnmarshaller.setIgnoreDuplicateRecordKeyValidationErrors(ignoreDuplicateRecordKeyValidationErrors);
		this.dataUnmarshaller.setRecordApplicationVersion(backupFileExtractor.getInfo().getCollectVersion());
	}
	
	private void initRecordUpdater() {
		this.recordUpdater = new RecordUpdater();
		this.recordUpdater.setValidateAfterUpdate(initializeRecords && validateRecords);
	}

	private void initializeRecordUserLoader() {
		this.recordUserLoader = new RecordUserLoader(userManager, activeUser, config.isCreateUsersFoundInRecords());
	}

	@Override
	public String getEntryName(int entryId, Step step) {
		if ( backupFileExtractor.isOldFormat() ) {
			return step.getStepNumber() + "/" + entryId + ".xml";
		} else {
			BackupRecordEntry recordEntry = new BackupRecordEntry(step, entryId);
			String entryName = recordEntry.getName();
			return entryName;
		}
	}

	@Override
	public ParseRecordResult provideRecordParsingResult(int entryId, Step step)
			throws IOException {
		String entryName = getEntryName(entryId, step);
		InputStream entryIS = backupFileExtractor.findEntryInputStream(entryName);
		if (entryIS == null) {
			return null;
		}
		InputStreamReader reader = OpenForisIOUtils.toReader(entryIS);
		ParseRecordResult parseRecordResult = parseRecord(reader, step);
		if (parseRecordResult.isSuccess()) {
			CollectRecord record = parseRecordResult.getRecord();
			recordUserLoader.adjustUserReferences(record);
			if (initializeRecords)
				recordUpdater.initializeRecord(record);
		}
		return parseRecordResult;
	}
	
	@Override
	public CollectRecord provideRecord(int entryId, Step step) throws IOException, RecordParsingException {
		ParseRecordResult parseResult = provideRecordParsingResult(entryId, step);
		if (parseResult == null) {
			return null;
		}
		if (parseResult.isSuccess()) {
			return parseResult.getRecord();
		} else {
			throw new RecordParsingException(parseResult, step);
		}
	}

	@Override
	public List<Integer> findEntryIds() {
		Set<Integer> result = new TreeSet<Integer>();
		for (Step step : Step.values()) {
			int stepNumber = step.getStepNumber();
			String path = (backupFileExtractor.isOldFormat() ? "" : SurveyBackupJob.DATA_FOLDER + SurveyBackupJob.ZIP_FOLDER_SEPARATOR) + stepNumber;
			if ( backupFileExtractor.containsEntriesInPath(path) ) {
				List<String> listEntriesInPath = backupFileExtractor.listFilesInFolder(path);
				for (String entry : listEntriesInPath) {
					String entryId = FilenameUtils.getBaseName(entry);
					result.add(Integer.parseInt(entryId));
				}
			}
		}
		return new ArrayList<Integer>(result);
	}
	
	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(backupFileExtractor);
	}
	
	private ParseRecordResult parseRecord(Reader reader, Step step) throws IOException {
		ParseRecordResult result = dataUnmarshaller.parse(reader);
		if (result.isSuccess()) {
			CollectRecord record = result.getRecord();
			record.setStep(step);
			record.setDataStep(step);
			record.setState(null); //ignore rejected information
		}
		// set correct step in failures/warnings
		for (NodeUnmarshallingError nodeUnmarshallingError : result.getWarnings()) {
			nodeUnmarshallingError.setStep(step);
		}
		for (NodeUnmarshallingError nodeUnmarshallingError : result.getFailures()) {
			nodeUnmarshallingError.setStep(step);
		}
		return result;
	}
	
	@Override
	public CollectSurvey getSurvey() {
		return packagedSurvey;
	}
	
	@Override
	public void setConfiguration(RecordProviderConfiguration config) {
		this.config = config;
		initializeRecordUserLoader();
	}
	
	public boolean isValidateRecords() {
		return validateRecords;
	}
	
	public void setValidateRecords(boolean validateRecords) {
		this.validateRecords = validateRecords;
		initDataUnmarshaller();
		initRecordUpdater();
	}
	
	public void setInitializeRecords(boolean initializeRecords) {
		this.initializeRecords = initializeRecords;
		initDataUnmarshaller();
		initRecordUpdater();
	}
	
	public static class RecordUserLoader {
		
		private static final String NEW_USER_PASSWORD = "password";
		
		private final UserManager userManager;
		private User activeUser;
		private Map<String, User> usersByName = new HashMap<String, User>();
		private boolean createNewUsers;
		
		public RecordUserLoader(UserManager userManager, User activeUser, boolean createNewUsers) {
			super();
			this.userManager = userManager;
			this.activeUser = activeUser;
			this.createNewUsers = createNewUsers;
		}

		public void adjustUserReferences(CollectRecord record) {
			User createdBy = record.getCreatedBy();
			if (createdBy != null) {
				User user = loadOrCreateAndInsertUser(createdBy.getUsername());
				record.setCreatedBy(user);
			}
			User modifiedBy = record.getModifiedBy();
			if (modifiedBy != null) {
				User user = loadOrCreateAndInsertUser(modifiedBy.getUsername());
				record.setModifiedBy(user);
			}
		}
	
		private User loadOrCreateAndInsertUser(String name) {
			User user;
			if ( StringUtils.isBlank(name) || userManager == null ) {
				return null;
			} else if ( usersByName.containsKey(name) ) {
				return usersByName.get(name);
			} else {
				user = userManager.loadByUserName(name);
				if ( user == null && createNewUsers ) {
					//create a user with data entry role and password equal to the user name
					try {
						user = userManager.insertUser(name, NEW_USER_PASSWORD, UserRole.ENTRY, activeUser);
					} catch (UserPersistenceException e) {
						throw new RuntimeException("Error creating new user with username '" + name + "'", e);
					}
				}
				usersByName.put(name, user);
				return user;
			}
		}
	}

}