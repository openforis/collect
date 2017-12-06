package org.openforis.collect.io.data;

import static org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy.ONLY_SPECIFIED;
import static org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy.OVERWRITE_ALL;
import static org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy.OVERWRITE_OLDER;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.openforis.collect.io.data.DataRestoreTask.OverwriteStrategy;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordManager.RecordOperations;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.User;
import org.openforis.commons.collection.Predicate;

public class RecordOperationGenerator {
	private final RecordProvider recordProvider;
	private final RecordManager recordManager;
	private final int entryId;
	private Predicate<CollectRecord> includeRecordPredicate;
	private User user;
	private OverwriteStrategy overwriteStrategy;

	public RecordOperationGenerator(RecordProvider recordProvider, RecordManager recordManager, 
			int entryId, User user, Predicate<CollectRecord> includeRecordPredicate, OverwriteStrategy overwriteStrategy) {
		super();
		this.recordProvider = recordProvider;
		this.recordManager = recordManager;
		this.entryId = entryId;
		this.user = user;
		this.includeRecordPredicate = includeRecordPredicate;
		this.overwriteStrategy = overwriteStrategy;
	}

	public RecordOperations generate() throws IOException, MissingStepsException, RecordParsingException {
		RecordOperations operations = new RecordOperations();
		boolean firstStepToBeProcessed = true;
		CollectRecordSummary existingRecordSummary = null;
		int workflowSequenceNumber = -1;
		boolean newRecord = true;

		for (Step step : Step.values()) {
			CollectRecord parsedRecord = recordProvider.provideRecord(entryId, step);
			if (parsedRecord == null || ! isToBeProcessed(parsedRecord)) {
				continue;
			}
			setDefaultValues(parsedRecord);

			if (firstStepToBeProcessed) {
				existingRecordSummary = findAlreadyExistingRecordSummary(parsedRecord);
				newRecord = existingRecordSummary == null;
				if (newRecord) {
					insertRecordDataUntilStep(operations, parsedRecord, step);
					workflowSequenceNumber = calculateStepDataSequenceNumber(existingRecordSummary, step);
				} else {
					Step existingRecordStep = existingRecordSummary.getStep();
					operations.initializeRecordId(existingRecordSummary.getId());
					operations.setOriginalStep(existingRecordStep);
					if (overwriteStrategy == OVERWRITE_OLDER && isNewer(parsedRecord, existingRecordSummary)
							|| overwriteStrategy == ONLY_SPECIFIED || overwriteStrategy == OVERWRITE_ALL) {
						// overwrite existing record data
						parsedRecord.setId(existingRecordSummary.getId());
						boolean insertNewDataStep = step.after(existingRecordStep);
						workflowSequenceNumber = calculateStepDataSequenceNumber(existingRecordSummary, step);
						operations.addUpdate(parsedRecord, step, insertNewDataStep, workflowSequenceNumber);
					}
				}
				firstStepToBeProcessed = false;
			} else {
				boolean insertNewDataStep = newRecord ? true : step.after(operations.getOriginalStep());
				workflowSequenceNumber = calculateStepDataSequenceNumber(existingRecordSummary, step);
				operations.addUpdate(parsedRecord, step, insertNewDataStep, workflowSequenceNumber);
			}
		}
		return operations;
	}
	
	private boolean isNewer(CollectRecord record, CollectRecordSummary existingSummary) {
		Date existingRecordModifiedDate = existingSummary.getSummaryByStep(record.getDataStep()).getModifiedDate();
		return record.getModifiedDate() != null && 
				(existingRecordModifiedDate == null || 
					record.getModifiedDate().compareTo(existingRecordModifiedDate) > 0
				);
	}

	private int calculateStepDataSequenceNumber(CollectRecordSummary existingRecordSummary, Step step) {
		if (existingRecordSummary == null) {
			return step.getStepNumber();
		} else if (step.after(existingRecordSummary.getStep())) {
			return existingRecordSummary.getCurrentStepSummary().getSequenceNumber() + 
					(step.getStepNumber() - existingRecordSummary.getStep().getStepNumber());
		} else {
			return existingRecordSummary.getSummaryByStep(step).getSequenceNumber(); 
		}
	}

	private void setDefaultValues(CollectRecord parsedRecord) {
		if (parsedRecord.getCreatedBy() == null) {
			parsedRecord.setCreatedBy(user);
		}
		if (parsedRecord.getModifiedBy() == null) {
			parsedRecord.setModifiedBy(user);
		}
		if (parsedRecord.getDataCreatedBy() == null) {
			parsedRecord.setDataCreatedBy(user);
		}
		if (parsedRecord.getDataModifiedBy() == null) {
			parsedRecord.setDataModifiedBy(user);
		}
		parsedRecord.setOwner(parsedRecord.getModifiedBy());
	}

	private boolean isToBeProcessed(CollectRecord record) {
		return includeRecordPredicate == null || includeRecordPredicate.evaluate(record);
	}

	private CollectRecordSummary findAlreadyExistingRecordSummary(
			CollectRecord parsedRecord) {
		CollectSurvey survey = (CollectSurvey) parsedRecord.getSurvey();
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(parsedRecord.getRootEntityDefinitionId());
		filter.setKeyValues(parsedRecord.getRootEntityKeyValues());
		List<CollectRecordSummary> summaries = recordManager.loadFullSummaries(filter, null);
		switch(summaries.size()) {
		case 0:
			return null;
		case 1:
			return summaries.get(0);
		default:
			throw new IllegalArgumentException(String.format("Multiple records with keys %s found for survey %s", 
					parsedRecord.getRootEntityKeyValues(), survey.getName()));
		}
	}

	private void insertRecordDataUntilStep(RecordOperations operations,
			CollectRecord record, Step untilStep) {
		List<Step> previousSteps = untilStep.getPreviousSteps();
		int dataStepSequenceNumber = 1;
		for (Step s : previousSteps) {
			record.setStep(s);
			switch (s) {
			case ENTRY:
				operations.addInsert(record);
				dataStepSequenceNumber ++;
				break;
			default:
				operations.addUpdate(record, s, true, dataStepSequenceNumber);
			}
		}
	}

}