package org.openforis.collect.io.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.io.data.DataImportSummary.FileErrorItem;
import org.openforis.collect.io.exception.DataParsingExeption;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataHandler.NodeUnmarshallingError;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.commons.collection.Predicate;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.model.Entity;
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
public class DataRestoreSummaryTask extends Task {

	private RecordManager recordManager;
	private UserManager userManager;

	//input
	private ZipFile zipFile;
	private boolean oldFormat;

	/**
	 * Survey packaged into the backup file
	 */
	private CollectSurvey packagedSurvey;
	
	/**
	 * Published survey already inserted into the system
	 */
	private CollectSurvey existingSurvey;
	
	/**
	 * If specified, it will be used to filter the records to include in the summary
	 */
	private Predicate<CollectRecord> includeRecordPredicate;

	//temporary instance variables
	private DataUnmarshaller dataUnmarshaller;
	private DataImportSummary summary;
	
	@Override
	protected long countTotalItems() {
		long total = 0;
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			if ( BackupRecordEntry.isValidRecordEntry(zipEntry, oldFormat) ) {
				total ++;
			}
		}
		return total;
	}
	
	@Override
	protected void execute() throws Throwable {
		summary = null;
		dataUnmarshaller = initDataUnmarshaller(packagedSurvey, existingSurvey);
		
		Map<Step, Integer> totalPerStep = new HashMap<CollectRecord.Step, Integer>();
		for (Step step : Step.values()) {
			totalPerStep.put(step, 0);
		}
		Map<Integer, CollectRecord> entryIdToRecord = new HashMap<Integer, CollectRecord>();
		Map<Integer, List<Step>> entryIdToSteps = new HashMap<Integer, List<Step>>();
		Map<String, List<NodeUnmarshallingError>> skippedEntryNameToErrors = new HashMap<String, List<NodeUnmarshallingError>>();
		Map<Integer, CollectRecord> entryIdToConflictingRecord = new HashMap<Integer, CollectRecord>();
		Map<Integer, Map<Step, List<NodeUnmarshallingError>>> entryIdToWarnings = new HashMap<Integer, Map<Step,List<NodeUnmarshallingError>>>();
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			if ( isRunning() ) {
				ZipEntry zipEntry = entries.nextElement();
				if ( ! BackupDataExtractor.BackupRecordEntry.isValidRecordEntry(zipEntry, oldFormat) ) {
					continue;
				}
				createSummaryForEntry(zipEntry, skippedEntryNameToErrors, 
						entryIdToRecord, entryIdToSteps, totalPerStep, 
						entryIdToConflictingRecord, entryIdToWarnings);
			} else {
				break;
			}
		}
		if ( isRunning() ) {
			String oldSurveyName = existingSurvey == null ? null: existingSurvey.getName();
			summary = createSummary(skippedEntryNameToErrors, oldSurveyName,
					totalPerStep, entryIdToRecord, entryIdToSteps,
					entryIdToConflictingRecord, entryIdToWarnings);
		}
	}

	private void createSummaryForEntry(ZipEntry zipEntry, 
			Map<String, List<NodeUnmarshallingError>> packagedSkippedFileErrors, Map<Integer, CollectRecord> packagedRecords, 
			Map<Integer, List<Step>> packagedStepsPerRecord, Map<Step, Integer> totalPerStep, 
			Map<Integer, CollectRecord> conflictingPackagedRecords, Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warnings) throws IOException, DataParsingExeption {
		String entryName = zipEntry.getName();
		BackupRecordEntry recordEntry = BackupRecordEntry.parse(entryName, oldFormat);
		Step step = recordEntry.getStep();
		InputStream is = zipFile.getInputStream(zipEntry);
		InputStreamReader reader = OpenForisIOUtils.toReader(is);
		ParseRecordResult parseRecordResult = parseRecord(reader);
		CollectRecord parsedRecord = parseRecordResult.getRecord();
		if ( ! parseRecordResult.isSuccess()) {
			List<NodeUnmarshallingError> failures = parseRecordResult.getFailures();
			packagedSkippedFileErrors.put(entryName, failures);
			incrementItemsSkipped();
		} else if ( includeRecordPredicate == null || includeRecordPredicate.evaluate(parsedRecord) ) {
			int entryId = recordEntry.getRecordId();
			CollectRecord recordSummary = createRecordSummary(parsedRecord);
			packagedRecords.put(entryId, recordSummary);
			List<Step> stepsPerRecord = packagedStepsPerRecord.get(entryId);
			if ( stepsPerRecord == null ) {
				stepsPerRecord = new ArrayList<CollectRecord.Step>();
				packagedStepsPerRecord.put(entryId, stepsPerRecord);
			}
			stepsPerRecord.add(step);
			Integer totalPerStep1 = totalPerStep.get(step);
			totalPerStep.put(step, totalPerStep1 + 1);
			CollectRecord oldRecord = findAlreadyExistingRecordSummary(parsedRecord);
			if ( oldRecord != null ) {
				conflictingPackagedRecords.put(entryId, oldRecord);
			}
			if ( parseRecordResult.hasWarnings() ) {
				Map<Step, List<NodeUnmarshallingError>> warningsPerEntry = warnings.get(entryId);
				if ( warningsPerEntry == null ) {
					warningsPerEntry = new HashMap<CollectRecord.Step, List<NodeUnmarshallingError>>();
					warnings.put(entryId, warningsPerEntry);
				}
				warningsPerEntry.put(step, parseRecordResult.getWarnings());
			}
			incrementItemsProcessed();
		}
	}
	
	private DataImportSummary createSummary(
			Map<String, List<NodeUnmarshallingError>> packagedSkippedFileErrors, 
			String surveyName,
			Map<Step, Integer> totalPerStep,
			Map<Integer, CollectRecord> packagedRecords,
			Map<Integer, List<Step>> packagedStepsPerRecord,
			Map<Integer, CollectRecord> conflictingPackagedRecords, 
			Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warnings) {
		DataImportSummary summary = new DataImportSummary();
		summary.setSurveyName(surveyName);
		
		List<DataImportSummaryItem> recordsToImport = new ArrayList<DataImportSummaryItem>();
		Set<Integer> entryIds = packagedRecords.keySet();
		for (Integer entryId: entryIds) {
			CollectRecord record = packagedRecords.get(entryId);
			if ( ! conflictingPackagedRecords.containsKey(entryId)) {
				List<Step> steps = packagedStepsPerRecord.get(entryId);
				DataImportSummaryItem item = new DataImportSummaryItem(entryId, record, steps);
				item.setWarnings(warnings.get(entryId));
				recordsToImport.add(item);
			}
		}
		summary.setRecordsToImport(recordsToImport);

		List<DataImportSummaryItem> conflictingRecordItems = new ArrayList<DataImportSummaryItem>();
		Set<Integer> conflictingEntryIds = conflictingPackagedRecords.keySet();
		for (Integer entryId: conflictingEntryIds) {
			CollectRecord record = packagedRecords.get(entryId);
			CollectRecord conflictingRecord = conflictingPackagedRecords.get(entryId);
			List<Step> steps = packagedStepsPerRecord.get(entryId);
			DataImportSummaryItem item = new DataImportSummaryItem(entryId, record, steps, conflictingRecord);
			item.setWarnings(warnings.get(entryId));
			conflictingRecordItems.add(item);
		}
		summary.setConflictingRecords(conflictingRecordItems);
		
		List<FileErrorItem> packagedSkippedFileErrorsList = new ArrayList<DataImportSummary.FileErrorItem>();
		Set<String> skippedFileNames = packagedSkippedFileErrors.keySet();
		for (String fileName : skippedFileNames) {
			List<NodeUnmarshallingError> nodeErrors = packagedSkippedFileErrors.get(fileName);
			FileErrorItem fileErrorItem = new FileErrorItem(fileName, nodeErrors);
			packagedSkippedFileErrorsList.add(fileErrorItem);
		}
		summary.setSkippedFileErrors(packagedSkippedFileErrorsList);
		summary.setTotalPerStep(totalPerStep);
		return summary;
	}

	private DataUnmarshaller initDataUnmarshaller(CollectSurvey packagedSurvey, CollectSurvey existingSurvey) throws SurveyImportException {
		CollectSurvey currentSurvey = existingSurvey == null ? packagedSurvey : existingSurvey;
		DataHandler handler = new DataHandler(userManager, currentSurvey, packagedSurvey);
		DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(handler);
		return dataUnmarshaller;
	}

	private CollectRecord findAlreadyExistingRecordSummary(CollectRecord parsedRecord) {
		CollectSurvey survey = (CollectSurvey) parsedRecord.getSurvey();
		List<String> keyValues = parsedRecord.getRootEntityKeyValues();
		Entity rootEntity = parsedRecord.getRootEntity();
		String rootEntityName = rootEntity.getName();
		List<CollectRecord> oldRecords = recordManager.loadSummaries(survey, rootEntityName, keyValues.toArray(new String[0]));
		if ( oldRecords == null || oldRecords.isEmpty() ) {
			return null;
		} else if ( oldRecords.size() == 1 ) {
			return oldRecords.get(0);
		} else {
			throw new IllegalStateException(String.format("Multiple records found in survey %s with key(s): %s", survey.getName(), keyValues));
		}
	}

	private ParseRecordResult parseRecord(Reader reader) throws IOException {
		ParseRecordResult result = dataUnmarshaller.parse(reader);
		return result;
	}

	private CollectRecord createRecordSummary(CollectRecord record) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		ModelVersion version = record.getVersion();
		String versionName = version != null ? version.getName(): null;
		CollectRecord result = new CollectRecord(survey, versionName);
		result.setCreatedBy(record.getCreatedBy());
		result.setCreationDate(record.getCreationDate());
		result.setEntityCounts(record.getEntityCounts());
		result.setErrors(record.getErrors());
		result.setId(record.getId());
		result.setMissing(record.getMissing());
		result.setModifiedBy(record.getModifiedBy());
		result.setModifiedDate(record.getModifiedDate());
		result.setRootEntityKeyValues(record.getRootEntityKeyValues());
		result.setSkipped(record.getSkipped());
		result.setState(record.getState());
		result.setStep(record.getStep());
		return result;
	}
	
	public RecordManager getRecordManager() {
		return recordManager;
	}
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public UserManager getUserManager() {
		return userManager;
	}
	
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
	
	public ZipFile getZipFile() {
		return zipFile;
	}
	
	public void setZipFile(ZipFile zipFile) {
		this.zipFile = zipFile;
	}
	
	public CollectSurvey getPackagedSurvey() {
		return packagedSurvey;
	}
	
	public void setPackagedSurvey(CollectSurvey packagedSurvey) {
		this.packagedSurvey = packagedSurvey;
	}
	
	public CollectSurvey getExistingSurvey() {
		return existingSurvey;
	}
	
	public void setExistingSurvey(CollectSurvey existingSurvey) {
		this.existingSurvey = existingSurvey;
	}
	
	public DataImportSummary getSummary() {
		return summary;
	}

	public boolean isOldFormat() {
		return oldFormat;
	}
	
	public void setOldFormat(boolean oldFormat) {
		this.oldFormat = oldFormat;
	}

	public Predicate<CollectRecord> getIncludeRecordPredicate() {
		return includeRecordPredicate;
	}
	
	public void setIncludeRecordPredicate(
			Predicate<CollectRecord> includeRecordPredicate) {
		this.includeRecordPredicate = includeRecordPredicate;
	}
	
}
