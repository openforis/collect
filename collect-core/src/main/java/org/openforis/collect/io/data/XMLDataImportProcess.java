package org.openforis.collect.io.data;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.zip.ZipException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.io.NewBackupFileExtractor;
import org.openforis.collect.io.data.DataImportState.MainStep;
import org.openforis.collect.io.data.DataImportState.SubStep;
import org.openforis.collect.io.data.DataImportSummary.FileErrorItem;
import org.openforis.collect.io.data.XMLParsingRecordProvider.RecordUserLoader;
import org.openforis.collect.io.exception.DataImportExeption;
import org.openforis.collect.io.exception.DataParsingExeption;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionRecordFileManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.persistence.jooq.JooqDaoSupport.CollectStoreQuery;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;
import org.openforis.commons.collection.Predicate;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.xml.IdmlParseException;
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
 * @deprecated use {@link DataRestoreJob} instead.  
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Deprecated
public class XMLDataImportProcess implements Callable<Void>, Closeable {

	private static final int MAX_QUERY_BUFFER_SIZE = 100;

	private static final Logger LOG = LogManager.getLogger(XMLDataImportProcess.class);

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private RecordFileManager recordFileManager;
	@Autowired
	private SessionRecordFileManager sessionRecordFileManager;
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

	/**
	 * Survey stored in the system with the same uri as the packaged one
	 */
	private CollectSurvey existingSurvey;
	private boolean overwriteAll;
	private DataUnmarshaller dataUnmarshaller;
	private List<Integer> processedRecords = new ArrayList<Integer>();
	private DataImportSummary summary;
	private List<Integer> entryIdsToImport = new ArrayList<Integer>();
	private boolean includesRecordFiles;
	private Predicate<CollectRecord> includeRecordPredicate;
	private boolean validateRecords = true;
	private List<CollectStoreQuery> queryBuffer = new ArrayList<CollectStoreQuery>();
	private Integer nextRecordId;
	private NewBackupFileExtractor backupFileExtractor;
	private RecordUserLoader recordUserLoader;

	@PostConstruct
	public void init() {
		this.state = new DataImportState();
		this.recordUserLoader = new RecordUserLoader(userManager, userManager.loadAdminUser(), true);
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
			beforeStart(); 
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
	
	public void callAndObserve(Observer observer) throws Exception{
		call();
		state.addObserver( observer );
	}
	
	@Override
	public void close() {
		IOUtils.closeQuietly(backupFileExtractor);
	}

	private void beforeStart() throws ZipException, IOException {
		if (backupFileExtractor == null) {
			backupFileExtractor = new NewBackupFileExtractor(file);
			backupFileExtractor.init();
		}
	}

	private void createDataImportSummary() throws DataImportExeption {
		try {
			state.setSubStep(SubStep.RUNNING);
			summary = null;
			packagedSurvey = extractPackagedSurvey();
			existingSurvey = loadExistingSurvey();

			validatePackagedSurvey();
			
			if (existingSurvey == null) {
				dataUnmarshaller = new DataUnmarshaller(packagedSurvey);
			} else {
				dataUnmarshaller = new DataUnmarshaller(existingSurvey, packagedSurvey);
			}
			
			Map<Step, Integer> totalPerStep = new HashMap<CollectRecord.Step, Integer>();
			for (Step step : Step.values()) {
				totalPerStep.put(step, 0);
			}
			Map<Integer, CollectRecord> packagedRecords = new HashMap<Integer, CollectRecord>();
			Map<Integer, List<Step>> packagedStepsPerRecord = new HashMap<Integer, List<Step>>();
			Map<String, List<NodeUnmarshallingError>> packagedSkippedFileErrors = new HashMap<String, List<NodeUnmarshallingError>>();
			Map<Integer, CollectRecordSummary> conflictingPackagedRecords = new HashMap<Integer, CollectRecordSummary>();
			Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warnings = new HashMap<Integer, Map<Step,List<NodeUnmarshallingError>>>();
			
			state.setTotal(backupFileExtractor.size());
			state.resetCount();
			
			List<String> entryNames = backupFileExtractor.getEntryNames();
			for (String entryName : entryNames) {
				if ( state.getSubStep() != DataImportState.SubStep.RUNNING ) {
					break;
				}
				if ( ! RecordEntry.isValidRecordEntry(entryName) ) {
					continue;
				}
				createSummaryForEntry(entryName, packagedSkippedFileErrors, 
						packagedRecords, packagedStepsPerRecord, totalPerStep, 
						conflictingPackagedRecords, warnings);
			}
			if ( state.getSubStep() == SubStep.RUNNING ) {
				String oldSurveyName = existingSurvey == null ? null: existingSurvey.getName();
				summary = createSummary(packagedSkippedFileErrors, oldSurveyName,
						totalPerStep, packagedRecords, packagedStepsPerRecord,
						conflictingPackagedRecords, warnings);
				state.setSubStep(DataImportState.SubStep.COMPLETE);
			}
			includesRecordFiles = backupFileExtractor.isIncludingRecordFiles();
		} catch (Exception e) {
			state.setSubStep(SubStep.ERROR);
			state.setErrorMessage(e.getMessage());
			LOG.error(e.getMessage(), e);
		}
	}

	private void validatePackagedSurvey() throws DataImportExeption {
		CollectSurvey publishedSurvey = getExistingSurvey();
		if ( packagedSurvey == null && publishedSurvey == null ) {
			throw new IllegalStateException("Published survey not found and " + XMLDataExportProcess.IDML_FILE_NAME + " not found in packaged file");
		} else if ( packagedSurvey == null ) {
			packagedSurvey = publishedSurvey;
		} else {
			String packagedSurveyUri = packagedSurvey.getUri();
			if ( surveyUri != null && !surveyUri.equals(packagedSurveyUri) ) {
				throw new IllegalArgumentException("Cannot import data related to survey '" + packagedSurveyUri + 
						"' into on a different survey (" + surveyUri + ")");
			}
			SurveyValidationResults compatibilityResult = surveyValidator.validateCompatibilityForDataImport(publishedSurvey, packagedSurvey);
			if ( compatibilityResult.hasErrors() ) {
				throw new DataImportExeption("Packaged survey is not compatible with the survey already present into the system.\n" +
						"Please try to import it using the Designer to get the list of errors.");
			}
		}
	}

	protected CollectSurvey loadExistingSurvey() {
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
	
	private void createSummaryForEntry(String entryName, 
			Map<String, List<NodeUnmarshallingError>> packagedSkippedFileErrors, 
			Map<Integer, CollectRecord> packagedRecords, 
			Map<Integer, List<Step>> packagedStepsPerRecord, 
			Map<Step, Integer> totalPerStep, 
			Map<Integer, CollectRecordSummary> conflictingPackagedRecords, 
			Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warnings) throws IOException, DataParsingExeption {
		RecordEntry recordEntry = RecordEntry.parse(entryName);
		Step step = recordEntry.getStep();
		InputStream is = backupFileExtractor.findEntryInputStream(entryName);
		ParseRecordResult parseRecordResult = parseRecord(is, false);
		CollectRecord parsedRecord = parseRecordResult.getRecord();
		if ( ! parseRecordResult.isSuccess()) {
			List<NodeUnmarshallingError> failures = parseRecordResult.getFailures();
			packagedSkippedFileErrors.put(entryName, failures);
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
			CollectRecordSummary oldRecord = findAlreadyExistingRecordSummary(parsedRecord);
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
			Map<Integer, CollectRecordSummary> conflictingPackagedRecords, 
			Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warnings) {
		DataImportSummary summary = new DataImportSummary();
		summary.setSurveyName(surveyName);
		
		List<DataImportSummaryItem> recordsToImport = new ArrayList<DataImportSummaryItem>();
		Set<Integer> entryIds = packagedRecords.keySet();
		for (Integer entryId: entryIds) {
			CollectRecord record = packagedRecords.get(entryId);
			if ( ! conflictingPackagedRecords.containsKey(entryId)) {
				List<Step> steps = packagedStepsPerRecord.get(entryId);
				DataImportSummaryItem item = new DataImportSummaryItem(entryId, CollectRecordSummary.fromRecord(record), steps);
				item.setWarnings(warnings.get(entryId));
				recordsToImport.add(item);
			}
		}
		List<DataImportSummaryItem> conflictingRecordItems = new ArrayList<DataImportSummaryItem>();
		Set<Integer> conflictingEntryIds = conflictingPackagedRecords.keySet();
		for (Integer entryId: conflictingEntryIds) {
			CollectRecord record = packagedRecords.get(entryId);
			CollectRecordSummary conflictingRecord = conflictingPackagedRecords.get(entryId);
			List<Step> steps = packagedStepsPerRecord.get(entryId);
			DataImportSummaryItem item = new DataImportSummaryItem(entryId, CollectRecordSummary.fromRecord(record),
					steps, conflictingRecord);
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
		try {
			state.setSubStep(DataImportState.SubStep.RUNNING);
			processedRecords = new ArrayList<Integer>();
			state.setTotal(entryIdsToImport.size());
			state.resetCount();
			state.setRunning(true);
			nextRecordId = recordManager.nextId();
			for (Integer entryId : entryIdsToImport) {
				if ( state.getSubStep() == SubStep.RUNNING && ! processedRecords.contains(entryId) ) {
					importEntries(entryId);
					processedRecords.add(entryId);
					state.incrementCount();
				} else {
					break;
				}
			}
			if ( state.getSubStep() == SubStep.RUNNING ) {
				flushQueryBuffer();
				state.setSubStep(SubStep.COMPLETE);
			}
		} catch (Exception e) {
			state.setError(true);
			state.setErrorMessage(e.getMessage());
			state.setSubStep(SubStep.ERROR);
			LOG.error("Error during data export", e);
		} finally {
			if (nextRecordId != null) {
				recordManager.restartIdSequence(nextRecordId);
			}
			state.setRunning(false);
			IOUtils.closeQuietly(backupFileExtractor);
		}
	}
	
	private void importEntries(int recordId) throws IOException, DataImportExeption, RecordPersistenceException {
		CollectRecord lastProcessedRecord = null;
		Step originalRecordStep = null;
		Step[] steps = Step.values();
		for (Step step : steps) {
			RecordEntry recordEntry = new RecordEntry(step, recordId);
			String entryName = recordEntry.getName();
			InputStream inputStream = backupFileExtractor.findEntryInputStream(entryName);
			if ( inputStream != null ) {
				ParseRecordResult parseRecordResult = parseRecord(inputStream, validateRecords);
				CollectRecord parsedRecord = parseRecordResult.getRecord();
				if (parsedRecord == null) {
					String message = parseRecordResult.getMessage();
					state.addError(entryName, message);
				} else {
					parsedRecord.setStep(step);
					List<CollectStoreQuery> queries;
					if ( lastProcessedRecord == null ) {
						CollectRecordSummary oldRecordSummary = findAlreadyExistingRecordSummary(parsedRecord);
						if (oldRecordSummary != null) {
							//overwrite existing record
							originalRecordStep = oldRecordSummary.getStep();
							parsedRecord.setId(oldRecordSummary.getId());
							if ( includesRecordFiles ) {
								recordFileManager.deleteAllFiles(parsedRecord);
							}
							int newWorkflowSequenceNumber = oldRecordSummary.getWorkflowSequenceNumber() + (step.getStepNumber() - originalRecordStep.getStepNumber());
							parsedRecord.setDataWorkflowSequenceNumber(newWorkflowSequenceNumber);
							parsedRecord.setWorkflowSequenceNumber(newWorkflowSequenceNumber);
							if (step.after(originalRecordStep)) {
								queries = Arrays.asList(recordManager.createDataInsertQuery(parsedRecord, oldRecordSummary.getId(), step, newWorkflowSequenceNumber));
							} else {
								queries = Arrays.asList(recordManager.createDataUpdateQuery(parsedRecord, oldRecordSummary.getId(), step, newWorkflowSequenceNumber));
							}
						} else {
							parsedRecord.setId(nextRecordId ++);
							queries = recordManager.createNewRecordInsertQueries(parsedRecord);
						}
						lastProcessedRecord = parsedRecord;
					} else {
						replaceData(parsedRecord, lastProcessedRecord);
						int sequenceNumber = parsedRecord.getWorkflowSequenceNumber() + 1;
						parsedRecord.setDataWorkflowSequenceNumber(sequenceNumber);
						if (step.after(originalRecordStep)) {
							queries = Arrays.asList(recordManager.createDataInsertQuery(lastProcessedRecord, lastProcessedRecord.getId(), step, sequenceNumber));
						} else {
							queries = Arrays.asList(recordManager.createDataUpdateQuery(lastProcessedRecord, lastProcessedRecord.getId(), step, sequenceNumber));
						}
					}
					appendQueries(queries);
//					if ( parseRecordResult.hasWarnings() ) {
//						state.addWarnings(entryName, parseRecordResult.getWarnings());
//					}
				}
			}
		}
		if ( lastProcessedRecord != null && originalRecordStep != null && originalRecordStep.compareTo(lastProcessedRecord.getStep()) > 0 ) {
			//reset the step to the original one and revalidate the record
			CollectSurvey survey = (CollectSurvey) lastProcessedRecord.getSurvey();
			CollectRecord originalRecord = recordManager.load(survey, lastProcessedRecord.getId(), originalRecordStep, validateRecords);
			originalRecord.setStep(originalRecordStep);
			afterRecordUpdate(originalRecord);
			appendQuery(recordManager.createSummaryUpdateQuery(originalRecord));
		}
		if ( includesRecordFiles ) {
			importRecordFiles(lastProcessedRecord);
		}
	}

	private void appendQuery(CollectStoreQuery query) {
		queryBuffer.add(query);
		if (queryBuffer.size() == MAX_QUERY_BUFFER_SIZE) {
			flushQueryBuffer();
		}
	}
	
	private void appendQueries(List<CollectStoreQuery> queries) {
		for (CollectStoreQuery query : queries) {
			appendQuery(query);
		}
	}

	private void flushQueryBuffer() {
		recordManager.execute(queryBuffer);
		queryBuffer.clear();
	}

	private void importRecordFiles(CollectRecord record) throws IOException, RecordPersistenceException {
		sessionRecordFileManager.resetTempInfo();
		List<FileAttribute> fileAttributes = record.getFileAttributes();
		String sessionId = "admindataimport";
		for (FileAttribute fileAttribute : fileAttributes) {
			String recordFileEntryName = XMLDataExportProcess.calculateRecordFileEntryName(fileAttribute);
			InputStream is = backupFileExtractor.findEntryInputStream(recordFileEntryName);
			if ( is != null ) {
				sessionRecordFileManager.saveToTempFile(is, fileAttribute.getFilename(), 
						record, fileAttribute.getInternalId());
			}
		}
		if ( sessionRecordFileManager.commitChanges(record) ) {
			if ( record.getStep() == Step.ANALYSIS ) {
				record.setStep(Step.CLEANSING);
				recordManager.save(record, sessionId);
				record.setStep(Step.ANALYSIS);
			}
			recordManager.save(record, sessionId);
		}
	}

	private CollectRecordSummary findAlreadyExistingRecordSummary(CollectRecord parsedRecord) {
		CollectSurvey survey = (CollectSurvey) parsedRecord.getSurvey();
		List<String> keyValues = parsedRecord.getRootEntityKeyValues();
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(parsedRecord.getRootEntityDefinitionId());
		filter.setKeyValues(keyValues);
		List<CollectRecordSummary> oldRecords = recordManager.loadSummaries(filter);
		if ( oldRecords == null || oldRecords.isEmpty() ) {
			return null;
		} else if ( oldRecords.size() == 1 ) {
			return oldRecords.get(0);
		} else {
			throw new IllegalStateException(String.format("Multiple records found in survey %s with key(s): %s", survey.getName(), keyValues));
		}
	}

	public CollectSurvey extractPackagedSurvey() throws IOException, IdmlParseException, DataImportExeption, SurveyValidationException {
		File idmlFile = backupFileExtractor.extractIdmlFile();
		if (idmlFile == null) {
			return null;
		}
		CollectSurvey survey = surveyManager.unmarshalSurvey(idmlFile, false, true);
		validateSurvey(survey);
		return survey;
	}

	private void validateSurvey(CollectSurvey survey) {
		SurveyValidationResults validationResults = surveyValidator.validate(survey);
		if ( validationResults.hasErrors() ) {
			throw new IllegalStateException("Packaged survey is not valid." +
					"\nPlease try to import it using the Designer to get the list of errors.");
		}
	}

	private ParseRecordResult parseRecord(InputStream is, boolean validateAndLoadReferences) throws IOException {
		dataUnmarshaller.setRecordDependencyGraphsEnabled(validateAndLoadReferences);
		ParseRecordResult result = dataUnmarshaller.parse(OpenForisIOUtils.toReader(is));
		if ( result.isSuccess() ) {
			CollectRecord record = result.getRecord();
			if (validateAndLoadReferences) {
				recordUserLoader.adjustUserReferences(record);
			}
		}
		return result;
	}

	private void afterRecordUpdate(CollectRecord record) {
		if (validateRecords) {
			try {
				recordManager.validate(record);
			} catch (Exception e) {
				LOG.info("Error validating record: " + record.getRootEntityKeyValues());
			}
		}
	}

	private void replaceData(CollectRecord fromRecord, CollectRecord toRecord) {
		toRecord.setCreatedBy(fromRecord.getCreatedBy());
		toRecord.setCreationDate(fromRecord.getCreationDate());
		toRecord.setModifiedBy(fromRecord.getModifiedBy());
		toRecord.setModifiedDate(fromRecord.getModifiedDate());
		toRecord.setStep(fromRecord.getStep());
		toRecord.setState(fromRecord.getState());
		toRecord.replaceRootEntity(fromRecord.getRootEntity());
		recordManager.validate(toRecord);
	}

	protected CollectRecord createRecordSummary(CollectRecord record) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		ModelVersion version = record.getVersion();
		String versionName = version != null ? version.getName(): null;
		CollectRecord result = new CollectRecord(survey, versionName, record.getRootEntity().getName(), false);
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

	public CollectSurvey getExistingSurvey() {
		return existingSurvey;
	}

	public CollectSurvey getPackagedSurvey() {
		return packagedSurvey;
	}
	
	public Predicate<CollectRecord> getIncludeRecordPredicate() {
		return includeRecordPredicate;
	}
	
	public void setIncludeRecordPredicate(Predicate<CollectRecord> includeRecordPredicate) {
		this.includeRecordPredicate = includeRecordPredicate;
	}

	public boolean isValidateRecords() {
		return validateRecords;
	}

	public void setValidateRecords(boolean validateRecords) {
		this.validateRecords = validateRecords;
	}
	
}
