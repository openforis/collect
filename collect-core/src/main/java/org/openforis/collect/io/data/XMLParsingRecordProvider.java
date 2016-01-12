package org.openforis.collect.io.data;

import java.io.Closeable;
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
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.concurrency.ProgressListener;

public class XMLParsingRecordProvider implements RecordProvider, Closeable {

	private final File file;
	private final CollectSurvey packagedSurvey;
	private final CollectSurvey existingSurvey;
	private final boolean validateRecords;
	
	//internal
	private NewBackupFileExtractor backupFileExtractor;
	private DataUnmarshaller dataUnmarshaller;
	private RecordUpdater recordUpdater;
	private RecordUserLoader recordUserLoader;
	
	public XMLParsingRecordProvider(File file, CollectSurvey packagedSurvey, 
			CollectSurvey existingSurvey, UserManager userManager, boolean validateRecords) {
		this.file = file;
		this.packagedSurvey = packagedSurvey;
		this.existingSurvey = existingSurvey;
		this.validateRecords = validateRecords;
		this.recordUserLoader = new RecordUserLoader(userManager);
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
		this.dataUnmarshaller.setRecordValidationEnabled(validateRecords);
		this.recordUpdater = new RecordUpdater();
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
		ParseRecordResult parseRecordResult = parseRecord(reader);
		return parseRecordResult;
	}
	
	@Override
	public CollectRecord provideRecord(int entryId, Step step) throws IOException, RecordParsingException {
		ParseRecordResult parseRecordResult = provideRecordParsingResult(entryId, step);
		if (parseRecordResult == null) {
			return null;
		}
		if (parseRecordResult.isSuccess()) {
			CollectRecord record = parseRecordResult.getRecord();
			recordUserLoader.adjustUserReferences(record);
			recordUpdater.initializeRecord(record, validateRecords);
			return record;
		} else {
			throw new RecordParsingException(parseRecordResult, step);
		}
	}

	@Override
	public List<Integer> findEntryIds() {
		Set<Integer> result = new TreeSet<Integer>();
		for (Step step : Step.values()) {
			int stepNumber = step.getStepNumber();
			String path = SurveyBackupJob.DATA_FOLDER + SurveyBackupJob.ZIP_FOLDER_SEPARATOR + stepNumber;
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
	
	private ParseRecordResult parseRecord(Reader reader) throws IOException {
		ParseRecordResult result = dataUnmarshaller.parse(reader);
		return result;
	}
	
	public static class RecordUserLoader {
		
		private static final String NEW_USER_PASSWORD = "password";
		
		private final UserManager userManager;
		private Map<String, User> usersByName;
		
		public RecordUserLoader(UserManager userManager) {
			super();
			this.userManager = userManager;
			this.usersByName = new HashMap<String, User>();
		}

		public void adjustUserReferences(CollectRecord record) {
			User createdBy = record.getCreatedBy();
			if (createdBy != null) {
				User user = loadOrCreateAndInsertUser(createdBy.getName());
				record.setCreatedBy(user);
			}
			User modifiedBy = record.getModifiedBy();
			if (modifiedBy != null) {
				User user = loadOrCreateAndInsertUser(modifiedBy.getName());
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
				if ( user == null ) {
					//create a user with data entry role and password equal to the user name
					try {
						user = userManager.insertUser(name, NEW_USER_PASSWORD, UserRole.ENTRY);
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