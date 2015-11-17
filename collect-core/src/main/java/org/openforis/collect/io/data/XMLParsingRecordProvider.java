package org.openforis.collect.io.data;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.NewBackupFileExtractor;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.commons.io.OpenForisIOUtils;

public class XMLParsingRecordProvider implements RecordProvider, Closeable {

	private final File file;
	private final CollectSurvey packagedSurvey;
	private final CollectSurvey existingSurvey;
	private final UserManager userManager;
	private final boolean validateRecords;
	
	//internal
	private NewBackupFileExtractor backupFileExtractor;
	private DataUnmarshaller dataUnmarshaller;
	private RecordUpdater recordUpdater;
	
	public XMLParsingRecordProvider(File file, CollectSurvey packagedSurvey, 
			CollectSurvey existingSurvey, UserManager userManager, boolean validateRecords) {
		this.file = file;
		this.packagedSurvey = packagedSurvey;
		this.existingSurvey = existingSurvey;
		this.userManager = userManager;
		this.validateRecords = validateRecords;
	}
	
	public void init() throws ZipException, IOException {
		this.backupFileExtractor = new NewBackupFileExtractor(file);
		this.dataUnmarshaller = initDataUnmarshaller();
		this.recordUpdater = new RecordUpdater();
	}
	
	@Override
	public ParseRecordResult provideRecordParsingResult(int entryId, Step step)
			throws IOException {
		String entryName = getBackupEntryName(entryId, step);
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
	
	private DataUnmarshaller initDataUnmarshaller() {
		CollectSurvey currentSurvey = existingSurvey == null ? packagedSurvey : existingSurvey;
		DataHandler handler = new DataHandler(userManager, currentSurvey, packagedSurvey, validateRecords);
		DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(handler);
		return dataUnmarshaller;
	}
	
	protected String getBackupEntryName(int entryId, Step step) {
		if ( backupFileExtractor.isOldFormat() ) {
			return step.getStepNumber() + "/" + entryId + ".xml";
		} else {
			BackupRecordEntry recordEntry = new BackupRecordEntry(step, entryId);
			String entryName = recordEntry.getName();
			return entryName;
		}
	}
	
}