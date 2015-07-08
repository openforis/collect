package org.openforis.collect.io.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.commons.io.OpenForisIOUtils;

public class XMLParsingRecordProvider implements RecordProvider {

	private final CollectSurvey packagedSurvey;
	private final CollectSurvey existingSurvey;
	private final UserManager userManager;
	
	//internal
	private final BackupFileExtractor backupFileExtractor;
	private final DataUnmarshaller dataUnmarshaller;
	private final HashMap<String, String> errorByEntryName;
	
	public XMLParsingRecordProvider(ZipFile zipFile, CollectSurvey packagedSurvey, 
			CollectSurvey existingSurvey, UserManager userManager) {
		this.packagedSurvey = packagedSurvey;
		this.existingSurvey = existingSurvey;
		this.userManager = userManager;
		
		//init internal variables
		this.backupFileExtractor = new BackupFileExtractor(zipFile);
		this.dataUnmarshaller = initDataUnmarshaller();
		this.errorByEntryName = new HashMap<String, String>();
	}
	
	@Override
	public CollectRecord provideRecord(int entryId, Step step) throws IOException {
		String entryName = getBackupEntryName(entryId, step);
		InputStream entryIS = backupFileExtractor.findEntryInputStream(entryName);
		if (entryIS == null) {
			return null;
		}
		InputStreamReader reader = OpenForisIOUtils.toReader(entryIS);
		ParseRecordResult parseRecordResult = parseRecord(reader);
		CollectRecord parsedRecord = parseRecordResult.getRecord();
		if (parsedRecord == null) {
			//error parsing record
			addError(entryName, parseRecordResult.getMessage());
			return null;
		} else {
			return parsedRecord;
		}
	}
	
	@Override
	public List<Integer> findEntryIds() {
		Set<Integer> result = new TreeSet<Integer>();
		for (Step step : Step.values()) {
			int stepNumber = step.getStepNumber();
			String path = SurveyBackupJob.DATA_FOLDER + SurveyBackupJob.ZIP_FOLDER_SEPARATOR + stepNumber;
			if ( backupFileExtractor.containsEntriesInPath(path) ) {
				List<String> listEntriesInPath = backupFileExtractor.listEntriesInPath(path);
				for (String entry : listEntriesInPath) {
					String entryId = FilenameUtils.getBaseName(entry);
					result.add(Integer.parseInt(entryId));
				}
			}
		}
		return new ArrayList<Integer>(result);
	}
	
	private ParseRecordResult parseRecord(Reader reader) throws IOException {
		ParseRecordResult result = dataUnmarshaller.parse(reader);
		return result;
	}
	
	private DataUnmarshaller initDataUnmarshaller() {
		CollectSurvey currentSurvey = existingSurvey == null ? packagedSurvey : existingSurvey;
		DataHandler handler = new DataHandler(userManager, currentSurvey, packagedSurvey);
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
	
	private void addError(String entryName, String message) {
		errorByEntryName.put(entryName, message);
	}

}