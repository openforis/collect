package org.openforis.collect.io.data;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.io.data.DataImportState.MainStep;
import org.openforis.collect.io.data.DataImportState.SubStep;
import org.openforis.collect.io.data.DataImportSummary.FileErrorItem;
import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.io.exception.DataParsingExeption;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataHandler.NodeUnmarshallingError;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.utils.OpenForisIOUtils;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 * 
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataImportProcess implements Callable<Void> {

	private static Log LOG = LogFactory.getLog(DataImportProcess.class);

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private RecordFileManager recordFileManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SurveyValidator surveyValidator;
	@Autowired
	private UserManager userManager;

	private String surveyUri;
	private DataImportState state;
	private File file;

	/**
	 * Survey contained into the package file
	 */
	private CollectSurvey packagedSurvey;

	private boolean overwriteAll;

	private DataUnmarshaller dataUnmarshaller;
	
	private List<Integer> processedRecords;

	private DataImportSummary summary;
	
	private List<Integer> entryIdsToImport;

	private boolean includesRecordFiles;

	public DataImportProcess() {
		super();
		this.state = new DataImportState();
		this.processedRecords = new ArrayList<Integer>();
		this.entryIdsToImport = new ArrayList<Integer>();
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
			default:
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
			validatePackagedSurvey();
			CollectSurvey existingSurvey = getExistingSurvey();
			dataUnmarshaller = initDataUnmarshaller(packagedSurvey, existingSurvey);
			
			Map<Step, Integer> totalPerStep = new HashMap<CollectRecord.Step, Integer>();
			for (Step step : Step.values()) {
				totalPerStep.put(step, 0);
			}
			Map<Integer, CollectRecord> packagedRecords = new HashMap<Integer, CollectRecord>();
			Map<Integer, List<Step>> packagedStepsPerRecord = new HashMap<Integer, List<Step>>();
			Map<String, List<NodeUnmarshallingError>> packagedSkippedFileErrors = new HashMap<String, List<NodeUnmarshallingError>>();
			Map<Integer, CollectRecord> conflictingPackagedRecords = new HashMap<Integer, CollectRecord>();
			Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warnings = new HashMap<Integer, Map<Step,List<NodeUnmarshallingError>>>();
			zipFile = new ZipFile(file);
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
				String oldSurveyName = existingSurvey == null ? null: existingSurvey.getName();
				summary = createSummary(packagedSkippedFileErrors, oldSurveyName,
						totalPerStep, packagedRecords, packagedStepsPerRecord,
						conflictingPackagedRecords, warnings);
				state.setSubStep(DataImportState.SubStep.COMPLETE);
			}
			includesRecordFiles = isIncludingRecordFiles(zipFile);
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

	private void validatePackagedSurvey() throws DataImportExeption {
		CollectSurvey existingSurvey = getExistingSurvey();
		if ( packagedSurvey == null && existingSurvey == null ) {
			throw new IllegalStateException("Published survey not found and " + XMLDataExportProcess.IDML_FILE_NAME + " not found in packaged file");
		} else if ( packagedSurvey == null ) {
			packagedSurvey = existingSurvey;
		} else {
			String packagedSurveyUri = packagedSurvey.getUri();
			if ( surveyUri != null && !surveyUri.equals(packagedSurveyUri) ) {
				throw new IllegalArgumentException("Cannot import data related to survey '" + packagedSurveyUri + 
						"' into on a different survey (" + surveyUri + ")");
			}
			List<SurveyValidationResult> compatibilityResult = surveyValidator.validateCompatibility(existingSurvey, packagedSurvey);
			if ( ! compatibilityResult.isEmpty() ) {
				throw new DataImportExeption("Packaged survey is not compatible with the survey already present into the system.\n" +
						"Please try to import it using the Designer to get the list of errors.");
			}
		}
	}

	protected CollectSurvey getExistingSurvey() {
		String uri;
		if ( surveyUri == null ) {
			if ( packagedSurvey == null ) {
				throw new IllegalStateException("Survey uri not specified and packaged survey not found");
			} else {
				uri = packagedSurvey.getUri();
			}
		} else {
			uri = surveyUri;
		}
		CollectSurvey survey = surveyManager.getByUri(uri);
		if ( survey == null && surveyUri != null ) {
			throw new IllegalArgumentException("Published survey not found. URI: " + surveyUri);
		} else {
			return survey;
		}
	}
	
	private void createSummaryForEntry(Enumeration<? extends ZipEntry> entries, ZipFile zipFile, 
			Map<String, List<NodeUnmarshallingError>> packagedSkippedFileErrors, Map<Integer, CollectRecord> packagedRecords, 
			Map<Integer, List<Step>> packagedStepsPerRecord, Map<Step, Integer> totalPerStep, 
			Map<Integer, CollectRecord> conflictingPackagedRecords, Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warnings) throws IOException, DataParsingExeption {
		ZipEntry zipEntry = (ZipEntry) entries.nextElement();
		if ( ! RecordEntry.isValidRecordEntry(zipEntry) ) {
			return;
		}
		String entryName = zipEntry.getName();
		RecordEntry recordEntry = RecordEntry.parse(entryName);
		Step step = recordEntry.getStep();
		InputStream is = zipFile.getInputStream(zipEntry);
		InputStreamReader reader = OpenForisIOUtils.toReader(is);
		ParseRecordResult parseRecordResult = parseRecord(reader);
		CollectRecord parsedRecord = parseRecordResult.getRecord();
		if ( ! parseRecordResult.isSuccess()) {
			List<NodeUnmarshallingError> failures = parseRecordResult.getFailures();
			packagedSkippedFileErrors.put(entryName, failures);
		} else {
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
			zipFile = new ZipFile(file);
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
	
	private void importEntries(ZipFile zipFile, int recordId) throws IOException, DataImportExeption, RecordPersistenceException {
		CollectRecord lastProcessedRecord = null;
		Step originalRecordStep = null;
		Step[] steps = Step.values();
		for (Step step : steps) {
			RecordEntry recordEntry = new RecordEntry(step, recordId);
			String entryName = recordEntry.getName();
			InputStream inputStream = getRecordEntryInputStream(zipFile, recordEntry);
			if ( inputStream != null ) {
				InputStreamReader reader = OpenForisIOUtils.toReader(inputStream);
				ParseRecordResult parseRecordResult = parseRecord(reader);
				CollectRecord parsedRecord = parseRecordResult.getRecord();
				if (parsedRecord == null) {
					String message = parseRecordResult.getMessage();
					state.addError(entryName, message);
				} else {
					parsedRecord.setStep(step);
					if ( lastProcessedRecord == null ) {
						CollectRecord oldRecordSummary = findAlreadyExistingRecordSummary(parsedRecord);
						if (oldRecordSummary == null) {
							//insert new record
							recordManager.save(parsedRecord);
							LOG.info("Inserted: " + parsedRecord.getId() + " (from file " + entryName + ")");
						} else {
							//overwrite existing record
							originalRecordStep = oldRecordSummary.getStep();
							parsedRecord.setId(oldRecordSummary.getId());
							if ( includesRecordFiles ) {
								recordFileManager.deleteAllFiles(parsedRecord);
							}
							recordManager.save(parsedRecord);
							LOG.info("Updated: " + oldRecordSummary.getId() + " (from file " + entryName  + ")");
						}
						lastProcessedRecord = parsedRecord;
					} else {
						replaceData(parsedRecord, lastProcessedRecord);
						recordManager.save(lastProcessedRecord);
					}
					if ( parseRecordResult.hasWarnings() ) {
						//state.addWarnings(entryName, parseRecordResult.getWarnings());
					}
				}
			}
		}
		if ( lastProcessedRecord != null && originalRecordStep != null && originalRecordStep.compareTo(lastProcessedRecord.getStep()) > 0 ) {
			//reset the step to the original one and revalidate the record
			CollectSurvey survey = (CollectSurvey) lastProcessedRecord.getSurvey();
			CollectRecord originalRecord = recordManager.load(survey, lastProcessedRecord.getId(), originalRecordStep);
			originalRecord.setStep(originalRecordStep);
			validateRecord(originalRecord);
			recordManager.save(originalRecord);
		}
		if ( includesRecordFiles ) {
			importRecordFiles(zipFile, lastProcessedRecord);
		}
	}

	private void importRecordFiles(ZipFile zipFile, CollectRecord record) throws IOException, RecordPersistenceException {
		recordFileManager.resetTempInfo();
		List<FileAttribute> fileAttributes = record.getFileAttributes();
		String sessionId = "admindataimport";
		for (FileAttribute fileAttribute : fileAttributes) {
			String recordFileEntryName = XMLDataExportProcess.calculateRecordFileEntryName(fileAttribute);
			InputStream is = getEntryInputStream(zipFile, recordFileEntryName);
			if ( is != null ) {
				recordFileManager.saveToTempFolder(is, fileAttribute.getFilename(), 
						sessionId, record, fileAttribute.getInternalId());
			}
		}
		if ( recordFileManager.commitChanges(sessionId, record) ) {
			if ( record.getStep() == Step.ANALYSIS ) {
				record.setStep(Step.CLEANSING);
				recordManager.save(record, sessionId);
				record.setStep(Step.ANALYSIS);
			}
			recordManager.save(record, sessionId);
		}
	}

	private InputStream getEntryInputStream(ZipFile zipFile, String entryName)
			throws IOException {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			if ( zipEntry.getName().equals(entryName) ) {
				return zipFile.getInputStream(zipEntry);
			}
		}
		return null;
	}
	
	private boolean isIncludingRecordFiles(ZipFile zipFile) {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			if ( zipEntry.getName().startsWith(XMLDataExportProcess.RECORD_FILE_DIRECTORY_NAME)) {
				return true;
			}
		}
		return false;
	}

	private InputStream getRecordEntryInputStream(ZipFile zipFile, RecordEntry entry) throws IOException, DataImportExeption {
		String entryName = entry.getName();
		InputStream result = getEntryInputStream(zipFile, entryName);
		return result;
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

	public CollectSurvey extractPackagedSurvey() throws IOException, IdmlParseException, DataImportExeption, SurveyValidationException {
		ZipFile zipFile = null;
		CollectSurvey survey = null;
		try {
			zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				if (zipEntry.isDirectory()) {
					continue;
				}
				String entryName = zipEntry.getName();
				if (XMLDataExportProcess.IDML_FILE_NAME.equals(entryName)) {
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
		return survey;
	}

	private ParseRecordResult parseRecord(Reader reader) throws IOException {
		ParseRecordResult result = dataUnmarshaller.parse(reader);
		if ( result.isSuccess() ) {
			CollectRecord record = result.getRecord();
			validateRecord(record);
			record.updateRootEntityKeyValues();
			record.updateEntityCounts();
		}
		return result;
	}

	private void validateRecord(CollectRecord record) {
		try {
			recordManager.validate(record);
		} catch (Exception e) {
			LOG.info("Error validating record: " + record.getRootEntityKeyValues());
		}
	}

	private void replaceData(CollectRecord fromRecord, CollectRecord toRecord) {
		toRecord.setCreatedBy(fromRecord.getCreatedBy());
		toRecord.setCreationDate(fromRecord.getCreationDate());
		toRecord.setModifiedBy(fromRecord.getModifiedBy());
		toRecord.setModifiedDate(fromRecord.getModifiedDate());
		toRecord.setStep(fromRecord.getStep());
		toRecord.setState(fromRecord.getState());
		toRecord.setRootEntity(fromRecord.getRootEntity());
		toRecord.updateRootEntityKeyValues();
		toRecord.updateEntityCounts();
		recordManager.validate(toRecord);
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

	public DataImportSummary getSummary() {
		return summary;
	}

	public List<Integer> getEntryIdsToImport() {
		return entryIdsToImport;
	}

	public void setEntryIdsToImport(List<Integer> entryIdsToImport) {
		this.entryIdsToImport = entryIdsToImport;
	}

	public String getSurveyUri() {
		return surveyUri;
	}

	public void setSurveyUri(String surveyUri) {
		this.surveyUri = surveyUri;
	}

	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}

}
