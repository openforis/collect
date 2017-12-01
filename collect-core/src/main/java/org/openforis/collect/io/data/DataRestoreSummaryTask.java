package org.openforis.collect.io.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.io.data.DataImportSummary.FileErrorItem;
import org.openforis.collect.io.exception.DataParsingExeption;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectRecordSummary.StepSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;
import org.openforis.collect.utils.Dates;
import org.openforis.commons.collection.Predicate;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.concurrency.Task;
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
	private RecordFileManager recordFileManager;
	private UserManager userManager;

	//input
	private CollectSurvey survey;
	private boolean oldFormat;
	private boolean fullSummary;

	/**
	 * If specified, it will be used to filter the records to include in the summary
	 * Ignored when the summary is not "full".
	 */
	private Predicate<CollectRecord> includeRecordPredicate;

	//temporary instance variables
	
	private RecordProvider recordProvider;
	
	@SuppressWarnings("serial")
	private final Map<Step, Integer> totalPerStep = new HashMap<Step, Integer>(){{
		for (Step step : Step.values()) {
			put(step, 0);
		}
	}};
	private final Map<Integer, CollectRecordSummary> recordSummaryByEntryId = new HashMap<Integer, CollectRecordSummary>();
	private final Map<Integer, Set<Step>> stepsByEntryId = new HashMap<Integer, Set<Step>>();
	private final Map<String, List<NodeUnmarshallingError>> errorsByEntryName = new HashMap<String, List<NodeUnmarshallingError>>();
	private final Map<Integer, CollectRecordSummary> conflictingRecordByEntryId = new HashMap<Integer, CollectRecordSummary>();
	private final Map<Integer, Map<Step, List<NodeUnmarshallingError>>> warningsByEntryId = new HashMap<Integer, Map<Step,List<NodeUnmarshallingError>>>();
	
	//output
	private DataImportSummary summary;
	private File dataSummaryFile;
	
	@Override
	protected long countTotalItems() {
		List<Integer> entryIds = extractEntryIdsToImport();
		return Step.values().length * entryIds.size();
	}

	@Override
	protected void execute() throws Throwable {
		if (fullSummary || dataSummaryFile == null) {
			List<Integer> idsToImport = extractEntryIdsToImport();
			for (Integer entryId : idsToImport) {
				if (! isRunning()) {
					break;
				}
				for (Step step : Step.values()) {
					if (! isRunning()) {
						break;
					}
					createSummaryForEntry(entryId, step);
				}
			}
		} else {
			CsvReader dataSummaryCSVReader = null;
			try {
				dataSummaryCSVReader = new CsvReader(dataSummaryFile);
				dataSummaryCSVReader.readHeaders();
				CsvLine line = dataSummaryCSVReader.readNextLine();
				while (line != null) {
					Integer entryId = line.getValue("entry_id", Integer.class);
					String stepVal = line.getValue("step", String.class);
					Step step = Step.valueOf(stepVal);
					
					CollectRecordSummary recordSummary = new CollectRecordSummary();
					recordSummary.setStep(step);
					Integer rootEntityDefId = line.getValue("root_entity_id", Integer.class);
					recordSummary.setRootEntityDefinitionId(rootEntityDefId);
					Date creationDate = Dates.parseDateTime(line.getValue("created_on", String.class));
					Date modifiedDate = Dates.parseDateTime(line.getValue("last_modified", String.class));
					recordSummary.setCreationDate(creationDate);
					recordSummary.setModifiedDate(modifiedDate);

					StepSummary stepSummary = new StepSummary(step);
					List<String> rootEntityKeyValues = Arrays.asList(
							line.getValue("key1", String.class),
							line.getValue("key2", String.class),
							line.getValue("key3", String.class)
					);
					stepSummary.setRootEntityKeyValues(rootEntityKeyValues);
					stepSummary.setCreationDate(creationDate);
					stepSummary.setModifiedDate(modifiedDate);
					recordSummary.addStepSummary(stepSummary);
					recordSummaryByEntryId.put(entryId, recordSummary);
					
					for (Step s : Step.values()) {
						if (s.beforeEqual(step)) {
							addStepPerEntry(entryId, s);
							incrementTotalPerStep(step);
							incrementProcessedItems();
						}
					}
					CollectRecordSummary oldRecord = findAlreadyExistingRecordSummary(entryId, step, rootEntityDefId, rootEntityKeyValues);
					if ( oldRecord != null ) {
						conflictingRecordByEntryId.put(entryId, oldRecord);
					}
					line = dataSummaryCSVReader.readNextLine();
				}
			} catch(Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(dataSummaryCSVReader);
			}
		}
	}
	
	@Override
	protected void afterExecuteInternal() {
		super.afterExecuteInternal();
		summary = createFinalSummary();
	}

	private void createSummaryForEntry(int entryId, Step step)
			throws IOException, DataParsingExeption {
		ParseRecordResult recordParsingResult = recordProvider.provideRecordParsingResult(entryId, step);
		if (recordParsingResult == null) {
			return;
		}
		CollectRecord parsedRecord = recordParsingResult.getRecord();
		if ( ! recordParsingResult.isSuccess()) {
			createParsingErrorSummary(entryId, step, recordParsingResult);
		} else if ( isToBeIncluded(parsedRecord) ) {
			createRecordParsedCorrectlySummary(entryId, step,
					recordParsingResult);
		}
	}

	private void createParsingErrorSummary(int entryId, Step step,
			ParseRecordResult parseRecordResult) {
		String entryName = getEntryName(entryId, step);
		List<NodeUnmarshallingError> failures = parseRecordResult.getFailures();
		errorsByEntryName.put(entryName, failures);
		incrementSkippedItems();
	}

	private void createRecordParsedCorrectlySummary(
			int entryId, Step step,
			ParseRecordResult parseRecordResult) {
		CollectRecord parsedRecord = parseRecordResult.getRecord();
		CollectRecordSummary recordSummary = CollectRecordSummary.fromRecord(parsedRecord);
		recordSummaryByEntryId.put(entryId, recordSummary);
		
		addStepPerEntry(entryId, step);
		
		CollectRecordSummary oldRecord = findAlreadyExistingFullRecordSummary(entryId, step, parsedRecord);
		if ( oldRecord != null ) {
			conflictingRecordByEntryId.put(entryId, oldRecord);
		}
		if ( parseRecordResult.hasWarnings() ) {
			addWarningsPerStep(entryId, step, parseRecordResult.getWarnings());
		}

		incrementTotalPerStep(step);
		incrementProcessedItems();
	}

	private void incrementTotalPerStep(Step step) {
		int oldTotal = totalPerStep.get(step);
		totalPerStep.put(step, oldTotal + 1);
	}

	private void addStepPerEntry(int entryId, Step step) {
		Set<Step> stepsPerRecord = stepsByEntryId.get(entryId);
		if ( stepsPerRecord == null ) {
			stepsPerRecord = new TreeSet<Step>();
			stepsByEntryId.put(entryId, stepsPerRecord);
		}
		stepsPerRecord.add(step);
	}

	private void addWarningsPerStep(int entryId, Step step,
			List<NodeUnmarshallingError> warnings) {
		Map<Step, List<NodeUnmarshallingError>> warningsPerEntry = warningsByEntryId.get(entryId);
		if ( warningsPerEntry == null ) {
			warningsPerEntry = new HashMap<Step, List<NodeUnmarshallingError>>();
			warningsByEntryId.put(entryId, warningsPerEntry);
		}
		warningsPerEntry.put(step, warnings);
	}
	
	private DataImportSummary createFinalSummary() {
		DataImportSummary summary = new DataImportSummary(fullSummary);
		summary.setSurveyName(survey == null ? null: survey.getName());
		summary.setRecordsToImport(createRecordToImportItems());
		summary.setSkippedFileErrors(createSkippedFileErrorItems());
		summary.setConflictingRecords(createConflictingRecordItems());
		summary.setTotalPerStep(totalPerStep);
		return summary;
	}

	private List<Integer> findIncompleteEntryIds() {
		List<Integer> result = new ArrayList<Integer>();
		for (Integer entryId: recordSummaryByEntryId.keySet()) {
			CollectRecordSummary conflictingRecord = conflictingRecordByEntryId.get(entryId);
			if (conflictingRecord != null) {
				Step lastStep = getLastStep(entryId);
				if (conflictingRecord.getStep().after(lastStep)) {
					result.add(entryId);
				}
			}
		}
		return result;
	}

	private Step getLastStep(Integer entryId) {
		Set<Step> steps = stepsByEntryId.get(entryId);
		return new ArrayList<Step>(steps).get(steps.size() - 1);
	}
	
	private List<FileErrorItem> createSkippedFileErrorItems() {
		List<FileErrorItem> errorItems = new ArrayList<FileErrorItem>();
		
		Set<String> skippedFileNames = errorsByEntryName.keySet();
		for (String fileName : skippedFileNames) {
			List<NodeUnmarshallingError> nodeErrors = errorsByEntryName.get(fileName);
			FileErrorItem fileErrorItem = new FileErrorItem(fileName, nodeErrors);
			errorItems.add(fileErrorItem);
		}

		List<Integer> incompleteEntryIds = findIncompleteEntryIds();
		for (Integer entryId : incompleteEntryIds) {
			CollectRecordSummary conflictingRecordSummary = conflictingRecordByEntryId.get(entryId);
			Step missingStep = conflictingRecordSummary.getStep();
			Set<Step> steps = stepsByEntryId.get(entryId);
			for (Step step : steps) {
				String entryName = getEntryName(entryId, step);
				if (! errorsByEntryName.containsKey(entryName)) {
					NodeUnmarshallingError error = new NodeUnmarshallingError("Incomplete entry set, missing step: " + missingStep);
					FileErrorItem fileErrorItem = new FileErrorItem(entryName, Arrays.asList(error));
					errorItems.add(fileErrorItem);
				}
			}
		}
		return errorItems;
	}

	private List<DataImportSummaryItem> createRecordToImportItems() {
		List<DataImportSummaryItem> recordsToImport = new ArrayList<DataImportSummaryItem>();
		List<Integer> incompleteEntryIds = findIncompleteEntryIds();
		Set<Integer> entryIds = recordSummaryByEntryId.keySet();
		for (Integer entryId: entryIds) {
			if ( ! conflictingRecordByEntryId.containsKey(entryId) && ! incompleteEntryIds.contains(entryId)) {
				Set<Step> steps = stepsByEntryId.get(entryId);
				CollectRecordSummary recordSummary = recordSummaryByEntryId.get(entryId);
				DataImportSummaryItem item = new DataImportSummaryItem(entryId, recordSummary, new ArrayList<Step>(steps));
				item.setWarnings(warningsByEntryId.get(entryId));
				recordsToImport.add(item);
			}
		}
		return recordsToImport;
	}

	private List<DataImportSummaryItem> createConflictingRecordItems() {
		List<DataImportSummaryItem> conflictingRecordItems = new ArrayList<DataImportSummaryItem>();
		List<Integer> incompleteEntryIds = findIncompleteEntryIds();
		Set<Integer> conflictingEntryIds = conflictingRecordByEntryId.keySet();
		for (Integer entryId: conflictingEntryIds) {
			if ( ! incompleteEntryIds.contains(entryId)) {
				CollectRecordSummary recordSummary = recordSummaryByEntryId.get(entryId);
				CollectRecordSummary conflictingRecord = conflictingRecordByEntryId.get(entryId);
				Set<Step> steps = stepsByEntryId.get(entryId);
				DataImportSummaryItem item = new DataImportSummaryItem(entryId, recordSummary, new ArrayList<Step>(steps), conflictingRecord);
				item.setWarnings(warningsByEntryId.get(entryId));
				conflictingRecordItems.add(item);
			}
		}
		return conflictingRecordItems;
	}

	private CollectRecordSummary findAlreadyExistingRecordSummary(int entryId, Step step, int rootEntityDefId, List<String> rootEntityKeys) {
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(rootEntityDefId);
		filter.setKeyValues(rootEntityKeys);
		List<CollectRecordSummary> summaries = recordManager.loadSummaries(filter);
		switch (summaries.size()) {
		case 0:
			return null;
		case 1:
			return summaries.get(0);
		default:
			String errorMessage = String.format("Data file: %s - multiple records found in survey %s with key(s) %s", 
					getEntryName(entryId, step), survey.getName(), rootEntityKeys);
			throw new IllegalStateException(errorMessage);
		}
	}
		
	private CollectRecordSummary findAlreadyExistingFullRecordSummary(int entryId, Step step, CollectRecord parsedRecord) {
		List<String> keyValues = parsedRecord.getRootEntityKeyValues();
		Entity rootEntity = parsedRecord.getRootEntity();
		int rootEntityDefId = rootEntity.getDefinition().getId();
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(rootEntityDefId);
		filter.setKeyValues(keyValues);
		List<CollectRecordSummary> oldRecordSummaries = recordManager.loadSummaries(filter);
		if ( oldRecordSummaries == null || oldRecordSummaries.isEmpty() ) {
			return null;
		} else if ( oldRecordSummaries.size() == 1 ) {
			CollectRecordSummary summary = oldRecordSummaries.get(0);
			CollectRecord record = recordManager.load(survey, summary.getId(), summary.getStep(), fullSummary);
			CollectRecordSummary recordSummary = CollectRecordSummary.fromRecord(record);
			recordSummary.setFiles(recordFileManager.getAllFiles(record));
			return recordSummary;
		} else {
			String errorMessage = String.format("Data file: %s - multiple records found in survey %s with key(s) %s", 
					getEntryName(entryId, step), survey.getName(), keyValues);
			throw new IllegalStateException(errorMessage);
		}
	}

	private String getEntryName(int entryId, Step step) {
		BackupRecordEntry recordEntry = new BackupRecordEntry(step, entryId, oldFormat);
		String entryName = recordEntry.getName();
		return entryName;
	}

	private List<Integer> extractEntryIdsToImport() {
		if (dataSummaryFile == null) { 
			return recordProvider.findEntryIds();
		} else {
			CsvReader dataSummaryCSVReader = null;
			try {
				dataSummaryCSVReader = new CsvReader(dataSummaryFile);
				dataSummaryCSVReader.readHeaders();
				List<Integer> entryIds = new ArrayList<Integer>(dataSummaryCSVReader.size());
				CsvLine line = dataSummaryCSVReader.readNextLine();
				while (line != null) {
					Integer entryId = line.getValue("entry_id", Integer.class);
					entryIds.add(entryId);
					line = dataSummaryCSVReader.readNextLine();
				}
				return entryIds;
			} catch(Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(dataSummaryCSVReader);
			}
		}
	}
	
	private boolean isToBeIncluded(CollectRecord recordSummary) {
		return includeRecordPredicate == null || includeRecordPredicate.evaluate(recordSummary);
	}

	public RecordManager getRecordManager() {
		return recordManager;
	}
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public void setRecordFileManager(RecordFileManager recordFileManager) {
		this.recordFileManager = recordFileManager;
	}
	
	public UserManager getUserManager() {
		return userManager;
	}
	
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
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

	public void setRecordProvider(RecordProvider recordProvider) {
		this.recordProvider = recordProvider;
	}

	public boolean isFullSummary() {
		return fullSummary;
	}

	public void setFullSummary(boolean fullSummary) {
		this.fullSummary = fullSummary;
	}

	public void setDataSummaryFile(File dataSummaryFile) {
		this.dataSummaryFile = dataSummaryFile;
	}

}
