package org.openforis.collect.remoting.service.dataImport;

import java.io.File;
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
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshallerException;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.model.Entity;

/**
 * 
 * @author S. Ricci
 * 
 */
public class DataImportProcess implements Callable<Void> {

	private static Log LOG = LogFactory.getLog(DataImportProcess.class);

	private static final Object IDML_FILE_NAME = "idml.xml";

	private RecordManager recordManager;
	private RecordDao recordDao;
	private SurveyManager surveyManager;

	private Map<String, User> users;
	private String newSurveyName;
	private DataImportState state;
	private File packagedFile;

	private CollectSurvey packagedSurvey;

	private boolean overwriteAll;

	private DataUnmarshaller dataUnmarshaller;
	
	private List<Integer> processedRecords;

	private DataImportSummary summary;
	
	private List<Integer> entryIdsToImport;
	
	public DataImportProcess(SurveyManager surveyManager, RecordManager recordManager, RecordDao recordDao, Map<String, User> users, File packagedFile, boolean overwriteAll) {
		super();
		this.surveyManager = surveyManager;
		this.recordManager = recordManager;
		this.recordDao = recordDao;
		this.users = users;
		this.packagedFile = packagedFile;
		this.overwriteAll = overwriteAll;
		this.state = new DataImportState();
		processedRecords = new ArrayList<Integer>();
		entryIdsToImport = new ArrayList<Integer>();
	}

	public DataImportState getState() {
		return state;
	}

	public void cancel() {
		state.setCancelled(true);
		state.setRunning(false);
		state.setStep(DataImportState.Step.CANCELLED);
	}

	public boolean isRunning() {
		return state.isRunning();
	}

	public boolean isComplete() {
		return state.isComplete();
	}

	public void init() throws DataImportExeption {
		try {
			state.reset();
			state.setStep(DataImportState.Step.PREPARE);
			summary = calculateDataImportSummary();
			state.setStep(DataImportState.Step.INITED);
		} catch (Exception e) {
			throw new DataImportExeption("Error initializing data import process", e);
		}
	}

	private DataImportSummary calculateDataImportSummary() throws DataImportExeption {
		try {
			packagedSurvey = extractPackagedSurvey();
			Map<String, String> packagedSkippedFileErrors = new HashMap<String, String>();
			String uri = packagedSurvey.getUri();
			CollectSurvey oldSurvey = surveyManager.getByUri(uri);
			boolean isNewSurvey = oldSurvey == null;
			dataUnmarshaller = initDataUnmarshaller(packagedSurvey, oldSurvey != null ? oldSurvey: packagedSurvey);
			
			Map<Step, Integer> totalPerStep = new HashMap<CollectRecord.Step, Integer>();
			for (Step step : Step.values()) {
				totalPerStep.put(step, 0);
			}
			Map<Integer, CollectRecord> packagedRecords = new HashMap<Integer, CollectRecord>();
			Map<Integer, List<Step>> packagedStepsPerRecord = new HashMap<Integer, List<Step>>();
			Map<Integer, CollectRecord> conflictingPackagedRecords = new HashMap<Integer, CollectRecord>();
			int total = 0;
			ZipFile zipFile = new ZipFile(packagedFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				String entryName = zipEntry.getName();
				if (zipEntry.isDirectory() || IDML_FILE_NAME.equals(entryName)) {
					continue;
				}
				Step step = getStep(entryName);
				InputStream inputStream = zipFile.getInputStream(zipEntry);
				InputStreamReader reader = new InputStreamReader(inputStream);
				ParseRecordResult parseRecordResult = parseRecord(reader);
				CollectRecord parsedRecord = parseRecordResult.record;
				String message = parseRecordResult.message;
				if (parsedRecord == null) {
					packagedSkippedFileErrors.put(entryName, message);
				} else {
					int packagedRecordId = getRecordId(entryName);
					CollectRecord recordSummary = createRecordSummary(parsedRecord);
					packagedRecords.put(packagedRecordId, recordSummary);
					List<Step> stepsPerRecord = packagedStepsPerRecord.get(packagedRecordId);
					if ( stepsPerRecord == null ) {
						stepsPerRecord = new ArrayList<CollectRecord.Step>();
						packagedStepsPerRecord.put(packagedRecordId, stepsPerRecord);
					}
					stepsPerRecord.add(step);
					Integer totalPerStep1 = totalPerStep.get(step);
					totalPerStep.put(step, totalPerStep1 + 1);
					CollectRecord oldRecord = findAlreadyExistingRecord(parsedRecord);
					if ( oldRecord != null ) {
						conflictingPackagedRecords.put(packagedRecordId, oldRecord);
					}
				}
				total++;
			}
			zipFile.close();
			state.setTotal(total);
			DataImportSummary summary = new DataImportSummary();
			summary.setNewSurvey(isNewSurvey);
			
			List<DataImportSummaryItem> recordsToImport = new ArrayList<DataImportSummaryItem>();
			Set<Integer> entryIds = packagedRecords.keySet();
			for (Integer entryId: entryIds) {
				CollectRecord record = packagedRecords.get(entryId);
				if ( ! conflictingPackagedRecords.containsKey(entryId)) {
					List<Step> steps = packagedStepsPerRecord.get(entryId);
					DataImportSummaryItem item = new DataImportSummaryItem(entryId, record, steps);
					recordsToImport.add(item);
				}
			}
			List<DataImportSummaryItem> conflictingRecordItems = new ArrayList<DataImportSummaryItem>();
			Set<Integer> conflictingEntryIds = conflictingPackagedRecords.keySet();
			for (Integer entryId: conflictingEntryIds) {
				CollectRecord record = packagedRecords.get(entryId);
				CollectRecord conflictingRecord = conflictingPackagedRecords.get(entryId);
				List<Step> steps = packagedStepsPerRecord.get(entryId);
				DataImportSummaryItem item = new DataImportSummaryItem(entryId, record, steps, conflictingRecord);
				conflictingRecordItems.add(item);
			}
			summary.setRecordsToImport(recordsToImport);
			summary.setConflictingRecords(conflictingRecordItems);
			summary.setSkippedFileErrors(packagedSkippedFileErrors);
			summary.setTotalPerStep(totalPerStep);
			return summary;
		} catch (Exception e) {
			throw new DataImportExeption(e.getMessage(), e);
		}
	}

	public void prepareToStart() {
		state.setStep(DataImportState.Step.STARTING);
	}

	@Override
	public Void call() throws Exception {
		try {
			String uri = packagedSurvey.getUri();
			CollectSurvey oldSurvey = surveyManager.getByUri(uri);
			state.setTotal(entryIdsToImport.size());
			if ( oldSurvey == null ) {
				packagedSurvey.setName(newSurveyName);
				surveyManager.importModel(packagedSurvey);
			}
			ZipFile zipFile = new ZipFile(packagedFile);
			state.setRunning(true);
			for (Integer entryId : entryIdsToImport) {
				if ( ! processedRecords.contains(entryId) ) {
					importEntries(zipFile, entryId);
				}
			}
			DataImportState.Step dataImportStep = state.getStep();
			if (! (dataImportStep == DataImportState.Step.CANCELLED || dataImportStep == DataImportState.Step.ERROR) ) {
				state.setStep(DataImportState.Step.COMPLETE);
			}
		} catch (Exception e) {
			state.setError(true);
			state.setErrorMessage(e.getMessage());
			state.setStep(DataImportState.Step.ERROR);
			LOG.error("Error during data export", e);
		} finally {
			state.setRunning(false);
		}
		return null;
	}
	
	private void importEntries(ZipFile zipFile, int recordId) throws IOException, DataImportExeption {
		Step[] steps = Step.values();
		state.setStep(DataImportState.Step.IMPORTING);
		CollectRecord lastStepRecord = null;
		Step oldRecordStep = null;
		for (Step step : steps) {
			String entryName = step.getStepNumber() + File.separator + recordId + ".xml";
			InputStream inputStream = getEntryInputStream(zipFile, recordId, step);
			if ( inputStream != null ) {
				InputStreamReader reader = new InputStreamReader(inputStream);
				ParseRecordResult parseRecordResult = parseRecord(reader);
				CollectRecord parsedRecord = parseRecordResult.record;
				String message = parseRecordResult.message;
				if (parsedRecord == null) {
					state.addError(entryName, message);
				} else {
					parsedRecord.setStep(step);
					if ( lastStepRecord == null ) {
						CollectRecord oldRecord = findAlreadyExistingRecord(parsedRecord);
						if (oldRecord != null) {
							oldRecordStep = oldRecord != null ? oldRecord.getStep(): null;
							lastStepRecord = recordDao.load((CollectSurvey) parsedRecord.getSurvey(), oldRecord.getId(), oldRecord.getStep().getStepNumber());
							replaceData(parsedRecord, lastStepRecord);
							recordDao.update(lastStepRecord);
							LOG.info("Updated: " + oldRecord.getId() + " (from file " + entryName  + ")");
						} else {
							recordDao.insert(parsedRecord);
							lastStepRecord = parsedRecord;
							LOG.info("Inserted: " + parsedRecord.getId() + " (from file " + entryName + ")");
						}
					} else {
						replaceData(parsedRecord, lastStepRecord);
						recordDao.update(lastStepRecord);
					}
				}
				if (!parseRecordResult.success) {
					if (parseRecordResult.warnings > 0) {
						state.addWarning(entryName, message);
					} else {
						state.addError(entryName, message);
					}
				}
			}
			if ( lastStepRecord != null && oldRecordStep != null && lastStepRecord.getStep() != oldRecordStep ) {
				lastStepRecord.setStep(oldRecordStep);
				lastStepRecord.updateDerivedStates();
				recordDao.update(lastStepRecord);
			}
		}
		processedRecords.add(recordId);
		state.incrementCount();
	}

	private InputStream getEntryInputStream(ZipFile zipFile, int recordId, Step step) throws IOException, DataImportExeption {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			String entryName = zipEntry.getName();
			if ( ! ( zipEntry.isDirectory() || IDML_FILE_NAME.equals(entryName) ) ) {
				Step entryStep = getStep(entryName);
				int entryRecordId = getRecordId(entryName);
				if ( entryStep == step && entryRecordId == recordId ) {
					return zipFile.getInputStream(zipEntry);
				}
			}
		}
		return null;
	}

	private DataUnmarshaller initDataUnmarshaller(CollectSurvey packagedSurvey, CollectSurvey existingSurvey) throws SurveyImportException {
		DataHandler handler = new DataHandler(existingSurvey, packagedSurvey, users);;
		DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(handler);
		return dataUnmarshaller;
	}

	private CollectRecord findAlreadyExistingRecord(CollectRecord parsedRecord) {
		CollectSurvey survey = (CollectSurvey) parsedRecord.getSurvey();
		List<String> keyValues = parsedRecord.getRootEntityKeyValues();
		Entity rootEntity = parsedRecord.getRootEntity();
		String rootEntityName = rootEntity.getName();
		List<CollectRecord> oldRecords = recordManager.loadSummaries(survey, rootEntityName, keyValues.toArray(new String[0]));
		if (oldRecords != null && oldRecords.size() == 1) {
			CollectRecord existingRecord = oldRecords.get(0);
			return existingRecord;
		}
		return null;
	}

	public CollectSurvey extractPackagedSurvey() throws IOException, InvalidIdmlException {
		ZipFile zipFile = new ZipFile(packagedFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			if (zipEntry.isDirectory()) {
				continue;
			}
			String entryName = zipEntry.getName();
			if (IDML_FILE_NAME.equals(entryName)) {
				InputStream is = zipFile.getInputStream(zipEntry);
				CollectSurvey survey = surveyManager.unmarshalSurvey(is);
				return survey;
			}
		}
		return null;
	}

	private ParseRecordResult parseRecord(Reader reader) throws IOException {
		ParseRecordResult result = new ParseRecordResult();
		try {
			CollectRecord record = dataUnmarshaller.parse(reader);
			recordManager.addEmptyNodes(record.getRootEntity());
			result.record = record;
			List<String> warns = dataUnmarshaller.getLastParsingWarnings();
			if (warns.size() > 0) {
				result.message = "Processed with errors: " + warns.toString();
				result.warnings = warns.size();
				result.success = false;
			} else {
				result.success = true;
				try {
					record.updateDerivedStates();
				} catch (Exception e) {
					LOG.info("Error validating record: " + record.getRootEntityKeyValues());
				}
				record.updateRootEntityKeyValues();
				record.updateEntityCounts();
			}
		} catch (DataUnmarshallerException e) {
			result.message = "Unable to process: " + e.getMessages().toString();
		} catch (RuntimeException e) {
			result.message = "Unable to process: " + e.toString();
		}
		return result;
	}

	private Step getStep(String zipEntryName) throws DataImportExeption {
		String[] entryNameSplitted = getEntryNameSplitted(zipEntryName);
		String stepNumStr = entryNameSplitted[0];
		int stepNumber = Integer.parseInt(stepNumStr);
		return Step.valueOf(stepNumber);
	}

	private int getRecordId(String zipEntryName) throws DataImportExeption {
		String[] entryNameSplitted = getEntryNameSplitted(zipEntryName);
		String fileName = entryNameSplitted[1];
		String[] fileNameSplitted = fileName.split(Pattern.quote("."));
		String recordId = fileNameSplitted[0];
		int result = Integer.parseInt(recordId);
		return result;
	}

	private String[] getEntryNameSplitted(String zipEntryName) throws DataImportExeption {
		String entryPathSeparator = Pattern.quote(File.separator);
		String[] entryNameSplitted = zipEntryName.split(entryPathSeparator);
		if (entryNameSplitted.length != 2) {
			entryPathSeparator = Pattern.quote("/");
			entryNameSplitted = zipEntryName.split(entryPathSeparator);
		}
		if (entryNameSplitted.length != 2) {
			throw new DataImportExeption("Packaged file format exception: wrong entry name: " + zipEntryName);
		}
		return entryNameSplitted;
	}
	
	private void replaceData(CollectRecord fromRecord, CollectRecord toRecord) {
		toRecord.setCreatedBy(fromRecord.getCreatedBy());
		toRecord.setCreationDate(fromRecord.getCreationDate());
		toRecord.setModifiedBy(fromRecord.getModifiedBy());
		toRecord.setModifiedDate(fromRecord.getModifiedDate());
		toRecord.setStep(fromRecord.getStep());
		toRecord.setState(fromRecord.getState());
		toRecord.setRootEntity(fromRecord.getRootEntity());
		toRecord.updateDerivedStates();
		toRecord.updateRootEntityKeyValues();
		toRecord.updateEntityCounts();
	}

	protected CollectRecord createRecordSummary(CollectRecord record) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		String versionName = record.getVersion().getName();
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
	
	public boolean isOverwriteAll() {
		return overwriteAll;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}

	public void setNewSurveyName(String newSurveyName) {
		this.newSurveyName = newSurveyName;
	}

	private class ParseRecordResult {
		private boolean success;
		private String message;
		private int warnings;
		private CollectRecord record;

		public ParseRecordResult() {
			success = false;
		}
	}

	public DataImportSummary getSummary() {
		return summary;
	}

	public List<Integer> getEntryIdsToImport() {
		return entryIdsToImport;
	}

	public void setEntryIdsToImport(List<Integer> entryIdsToImport) {
		this.entryIdsToImport = entryIdsToImport;
	}

}
