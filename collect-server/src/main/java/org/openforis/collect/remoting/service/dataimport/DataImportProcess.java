package org.openforis.collect.remoting.service.dataimport;

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
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataHandler.NodeUnmarshallingError;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.remoting.service.dataimport.DataImportState.MainStep;
import org.openforis.collect.remoting.service.dataimport.DataImportState.SubStep;
import org.openforis.collect.remoting.service.dataimport.DataImportSummary.FileErrorItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Entity;
import org.springframework.transaction.annotation.Transactional;

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
	private SurveyValidator surveyValidator;

	private Map<String, User> users;
	private String selectedSurveyUri;
	private String newSurveyName;
	private DataImportState state;
	private File packagedFile;

	/**
	 * Survey contained into the package file
	 */
	private CollectSurvey packagedSurvey;

	private boolean overwriteAll;

	private DataUnmarshaller dataUnmarshaller;
	
	private List<Integer> processedRecords;

	private DataImportSummary summary;
	
	private List<Integer> entryIdsToImport;

	
	public DataImportProcess(SurveyManager surveyManager, SurveyValidator surveyValidator, RecordManager recordManager, RecordDao recordDao,
			String selectedSurveyUri, Map<String, User> users, File packagedFile, boolean overwriteAll) {
		super();
		this.surveyManager = surveyManager;
		this.surveyValidator = surveyValidator;
		this.recordManager = recordManager;
		this.recordDao = recordDao;
		this.selectedSurveyUri = selectedSurveyUri;
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
		if ( state.getSubStep() == DataImportState.SubStep.RUNNING) {
			state.setSubStep(DataImportState.SubStep.CANCELLED);
		}
	}

	public boolean isRunning() {
		return state.isRunning();
	}

	public boolean isComplete() {
		return state.isComplete();
	}

	@Override
	public Void call() throws Exception {
		if ( state.getSubStep() == SubStep.PREPARING ) {
			switch ( state.getMainStep() ) {
			case SUMMARY_CREATION:
				createDataImportSummary();
				break;
			case IMPORT:
				importPackagedFile();
				break;
			}
		}
		return null;
	}

	private void createDataImportSummary() throws DataImportExeption {
		ZipFile zipFile = null;
		try {
			state.setSubStep(SubStep.RUNNING);
			summary = null;
			packagedSurvey = extractPackagedSurvey();
			CollectSurvey oldSurvey = getOldSurvey();
			String packagedSurveyUri = packagedSurvey.getUri();
			if ( selectedSurveyUri != null && !selectedSurveyUri.equals(packagedSurveyUri) ) {
				throw new IllegalArgumentException("Cannot import data related to survey '" + packagedSurveyUri + 
						"' into on a different survey (" + selectedSurveyUri + ")");
			}
			if ( oldSurvey != null ) {
				List<SurveyValidationResult> compatibilityResult = surveyValidator.validateCompatibility(oldSurvey, packagedSurvey);
				if ( ! compatibilityResult.isEmpty() ) {
					throw new DataImportExeption("Packaged survey is not compatible with the survey already present into the system.\n" +
							"Please try to import it using the Designer to get the list of errors.");
				}
			}
			dataUnmarshaller = initDataUnmarshaller(packagedSurvey, oldSurvey);
			
			Map<Step, Integer> totalPerStep = new HashMap<CollectRecord.Step, Integer>();
			for (Step step : Step.values()) {
				totalPerStep.put(step, 0);
			}
			Map<Integer, CollectRecord> packagedRecords = new HashMap<Integer, CollectRecord>();
			Map<Integer, List<Step>> packagedStepsPerRecord = new HashMap<Integer, List<Step>>();
			Map<String, List<NodeUnmarshallingError>> packagedSkippedFileErrors = new HashMap<String, List<NodeUnmarshallingError>>();
			Map<Integer, CollectRecord> conflictingPackagedRecords = new HashMap<Integer, CollectRecord>();
			Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warnings = new HashMap<Integer, Map<Step,List<NodeUnmarshallingError>>>();
			zipFile = new ZipFile(packagedFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			state.setTotal(zipFile.size());
			state.resetCount();
			while (entries.hasMoreElements()) {
				if ( state.getSubStep() == DataImportState.SubStep.RUNNING ) {
					createSummaryForEntry(entries, zipFile, packagedSkippedFileErrors, 
							packagedRecords, packagedStepsPerRecord, totalPerStep, 
							conflictingPackagedRecords, warnings);
				} else {
					break;
				}
			}
			if ( state.getSubStep() == SubStep.RUNNING ) {
				String oldSurveyName = oldSurvey == null ? null: oldSurvey.getName();
				summary = createSummary(packagedSkippedFileErrors, oldSurveyName,
						totalPerStep, packagedRecords, packagedStepsPerRecord,
						conflictingPackagedRecords, warnings);
				state.setSubStep(DataImportState.SubStep.COMPLETE);
			}
		} catch (Exception e) {
			state.setSubStep(SubStep.ERROR);
			state.setErrorMessage(e.getMessage());
			LOG.error(e.getMessage(), e);
		} finally {
			if ( zipFile != null ) {
				try {
					zipFile.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	protected CollectSurvey getOldSurvey() {
		String uri;
		if ( selectedSurveyUri == null ) {
			uri = packagedSurvey.getUri();
		} else {
			uri = selectedSurveyUri;
		}
		CollectSurvey survey = surveyManager.getByUri(uri);
		if ( survey == null && selectedSurveyUri != null ) {
			throw new IllegalArgumentException("Published survey not found. URI: " + selectedSurveyUri);
		} else {
			return survey;
		}
	}
	
	private void createSummaryForEntry(Enumeration<? extends ZipEntry> entries, ZipFile zipFile, 
			Map<String, List<NodeUnmarshallingError>> packagedSkippedFileErrors, Map<Integer, CollectRecord> packagedRecords, 
			Map<Integer, List<Step>> packagedStepsPerRecord, Map<Step, Integer> totalPerStep, 
			Map<Integer, CollectRecord> conflictingPackagedRecords, Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warnings) throws DataImportExeption, IOException {
		ZipEntry zipEntry = (ZipEntry) entries.nextElement();
		String entryName = zipEntry.getName();
		if (zipEntry.isDirectory() || IDML_FILE_NAME.equals(entryName)) {
			return;
		}
		Step step = getStep(entryName);
		InputStream inputStream = zipFile.getInputStream(zipEntry);
		InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
		ParseRecordResult parseRecordResult = parseRecord(reader);
		CollectRecord parsedRecord = parseRecordResult.getRecord();
		if ( ! parseRecordResult.isSuccess()) {
			List<NodeUnmarshallingError> failures = parseRecordResult.getFailures();
			packagedSkippedFileErrors.put(entryName, failures);
		} else {
			int entryId = getRecordId(entryName);
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
			CollectRecord oldRecord = findAlreadyExistingRecord(parsedRecord);
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
		}
		state.incrementCount();
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
		summary.setRecordsToImport(recordsToImport);
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

	public void prepareToStartSummaryCreation() {
		state.setMainStep(MainStep.SUMMARY_CREATION);
		state.setSubStep(DataImportState.SubStep.PREPARING);
	}

	public void prepareToStartImport() {
		state.setMainStep(MainStep.IMPORT);
		state.setSubStep(DataImportState.SubStep.PREPARING);
	}

	@Transactional
	protected void importPackagedFile() {
		ZipFile zipFile = null;
		try {
			state.setSubStep(DataImportState.SubStep.RUNNING);
			processedRecords = new ArrayList<Integer>();
			state.setTotal(entryIdsToImport.size());
			state.resetCount();
			CollectSurvey oldSurvey = getOldSurvey();
			if ( oldSurvey == null ) {
				packagedSurvey.setName(newSurveyName);
				surveyManager.importModel(packagedSurvey);
			}
			zipFile = new ZipFile(packagedFile);
			state.setRunning(true);
			for (Integer entryId : entryIdsToImport) {
				if ( state.getSubStep() == SubStep.RUNNING && ! processedRecords.contains(entryId) ) {
					importEntries(zipFile, entryId);
					processedRecords.add(entryId);
					state.incrementCount();
				} else {
					break;
				}
			}
			if ( state.getSubStep() == SubStep.RUNNING ) {
				state.setSubStep(SubStep.COMPLETE);
			}
		} catch (Exception e) {
			state.setError(true);
			state.setErrorMessage(e.getMessage());
			state.setSubStep(SubStep.ERROR);
			LOG.error("Error during data export", e);
		} finally {
			state.setRunning(false);
			if ( zipFile != null ) {
				try {
					zipFile.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
	
	private void importEntries(ZipFile zipFile, int entryId) throws IOException, DataImportExeption {
		CollectRecord lastStepRecord = null;
		Step oldRecordStep = null;
		Step[] steps = Step.values();
		for (Step step : steps) {
			String entryName = step.getStepNumber() + File.separator + entryId + ".xml";
			InputStream inputStream = getEntryInputStream(zipFile, entryId, step);
			if ( inputStream != null ) {
				InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
				ParseRecordResult parseRecordResult = parseRecord(reader);
				CollectRecord parsedRecord = parseRecordResult.getRecord();
				String message = parseRecordResult.getMessage();
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
					if ( parseRecordResult.hasWarnings() ) {
						//state.addWarnings(entryName, parseRecordResult.getWarnings());
					}
				}
			}
			if ( lastStepRecord != null && oldRecordStep != null && lastStepRecord.getStep() != oldRecordStep ) {
				lastStepRecord.setStep(oldRecordStep);
				recordManager.validate(lastStepRecord);
				recordDao.update(lastStepRecord);
			}
		}
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
		CollectSurvey currentSurvey = existingSurvey == null ? packagedSurvey : existingSurvey;
		DataHandler handler = new DataHandler(currentSurvey, packagedSurvey, users);;
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

	public CollectSurvey extractPackagedSurvey() throws IOException, IdmlParseException, DataImportExeption, SurveyValidationException {
		ZipFile zipFile = null;
		CollectSurvey survey = null;
		try {
			zipFile = new ZipFile(packagedFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				if (zipEntry.isDirectory()) {
					continue;
				}
				String entryName = zipEntry.getName();
				if (IDML_FILE_NAME.equals(entryName)) {
					InputStream is = zipFile.getInputStream(zipEntry);
					survey = surveyManager.unmarshalSurvey(is);
					List<SurveyValidationResult> validationResults = surveyValidator.validate(survey);
					if ( ! validationResults.isEmpty() ) {
						throw new IllegalStateException("Packaged survey is not valid." +
								"\nPlease try to import it using the Designer to get the list of errors.");
					}
				}
			}
		} finally {
			if ( zipFile != null) {
				zipFile.close();
			}
		}
		if ( survey == null ) {
			throw new DataImportExeption(IDML_FILE_NAME + " not found in packaged file.");
		}
		return survey;
	}

	private ParseRecordResult parseRecord(Reader reader) throws IOException {
		ParseRecordResult result = dataUnmarshaller.parse(reader);
		if ( result.isSuccess() ) {
			CollectRecord record = result.getRecord();
			try {
				recordManager.validate(record);
			} catch (Exception e) {
				LOG.info("Error validating record: " + record.getRootEntityKeyValues());
			}
			record.updateRootEntityKeyValues();
			record.updateEntityCounts();
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
		recordManager.validate(toRecord);
		toRecord.updateRootEntityKeyValues();
		toRecord.updateEntityCounts();
	}

	protected CollectRecord createRecordSummary(CollectRecord record) {
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
	
	public boolean isOverwriteAll() {
		return overwriteAll;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}

	public void setNewSurveyName(String newSurveyName) {
		this.newSurveyName = newSurveyName;
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
