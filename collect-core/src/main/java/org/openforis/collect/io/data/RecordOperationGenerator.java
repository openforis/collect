package org.openforis.collect.io.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	public RecordOperationGenerator(RecordProvider recordProvider,
			RecordManager recordManager, int entryId, User user, Predicate<CollectRecord> includeRecordPredicate) {
		super();
		this.recordProvider = recordProvider;
		this.recordManager = recordManager;
		this.entryId = entryId;
		this.user = user;
		this.includeRecordPredicate = includeRecordPredicate;
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
					workflowSequenceNumber = step.getStepNumber();
				} else {
					// overwrite existing record data
					Step originalStep = existingRecordSummary.getStep();
					parsedRecord.setId(existingRecordSummary.getId());
					operations.initializeRecordId(existingRecordSummary.getId());
					operations.setOriginalStep(originalStep);
					boolean newStep = step.after(originalStep);
					if (newStep) {
						workflowSequenceNumber = existingRecordSummary.getWorkflowSequenceNumber() + (step.getStepNumber() - originalStep.getStepNumber());
					} else {
						workflowSequenceNumber = existingRecordSummary.getSummaryByStep(step).getSequenceNumber(); 
					}
					operations.addUpdate(parsedRecord, step, newStep, workflowSequenceNumber);
				}
				firstStepToBeProcessed = false;
			} else {
				boolean newStep = newRecord ? true : step.after(operations.getOriginalStep());
				operations.addUpdate(parsedRecord, step, newStep, workflowSequenceNumber);
			}
			workflowSequenceNumber ++;
		}
		return operations;
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
		List<Step> previousSteps = new ArrayList<Step>();
		for (Step s : Step.values()) {
			if (s.beforeEqual(untilStep)) {
				previousSteps.add(s);
			}
		}
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