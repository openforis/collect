package org.openforis.collect.io.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openforis.collect.io.data.BackupDataExtractor.BackupRecordEntry;
import org.openforis.collect.io.data.DataImportSummary.FileErrorItem;
import org.openforis.collect.io.exception.DataParsingExeption;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataUnmarshaller.ParseRecordResult;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;
import org.openforis.commons.collection.Predicate;
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
	private UserManager userManager;

	//input
	private CollectSurvey survey;
	private boolean oldFormat;
	private boolean completeSummary;

	/**
	 * If specified, it will be used to filter the records to include in the summary
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
	
	@Override
	protected long countTotalItems() {
		List<Integer> entryIds = recordProvider.findEntryIds();
		return Step.values().length * entryIds.size();
	}
	
	@Override
	protected void execute() throws Throwable {
		List<Integer> idsToImport = recordProvider.findEntryIds();
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
		
		CollectRecordSummary oldRecord = findAlreadyExistingRecordSummary(parsedRecord);
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
		DataImportSummary summary = new DataImportSummary();
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

	private CollectRecordSummary findAlreadyExistingRecordSummary(CollectRecord parsedRecord) {
		CollectSurvey survey = (CollectSurvey) parsedRecord.getSurvey();
		List<String> keyValues = parsedRecord.getRootEntityKeyValues();
		Entity rootEntity = parsedRecord.getRootEntity();
		String rootEntityName = rootEntity.getName();
		List<CollectRecord> oldRecords = recordManager.loadSummaries(survey, rootEntityName, keyValues.toArray(new String[keyValues.size()]));
		if ( oldRecords == null || oldRecords.isEmpty() ) {
			return null;
		} else if ( oldRecords.size() == 1 ) {
			CollectRecord summary = oldRecords.get(0);
			CollectRecord record = recordManager.load(survey, summary.getId(), summary.getStep(), completeSummary);
			return CollectRecordSummary.fromRecord(record);
		} else {
			throw new IllegalStateException(String.format("Multiple records found in survey %s with key(s): %s", survey.getName(), keyValues));
		}
	}

	private String getEntryName(int entryId, Step step) {
		BackupRecordEntry recordEntry = new BackupRecordEntry(step, entryId, oldFormat);
		String entryName = recordEntry.getName();
		return entryName;
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

	public boolean isCompleteSummary() {
		return completeSummary;
	}

	public void setCompleteSummary(boolean completeSummary) {
		this.completeSummary = completeSummary;
	}

}
