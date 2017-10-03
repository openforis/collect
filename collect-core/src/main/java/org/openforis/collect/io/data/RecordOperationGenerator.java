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
import org.openforis.commons.collection.Predicate;

public class RecordOperationGenerator {
	private final RecordProvider recordProvider;
	private final RecordManager recordManager;
	private final int entryId;
	private Predicate<CollectRecord> includeRecordPredicate;

	public RecordOperationGenerator(RecordProvider recordProvider,
			RecordManager recordManager, int entryId, Predicate<CollectRecord> includeRecordPredicate) {
		super();
		this.recordProvider = recordProvider;
		this.recordManager = recordManager;
		this.entryId = entryId;
		this.includeRecordPredicate = includeRecordPredicate;
	}

	public RecordOperations generate() throws IOException, MissingStepsException, RecordParsingException {
		RecordOperations operations = new RecordOperations();
		boolean firstStepToBeProcessed = true;
		Integer lastRecordId = null;
		Step lastRecordOriginalStep = null;
		for (Step step : Step.values()) {
			CollectRecord parsedRecord = recordProvider.provideRecord(entryId, step);
			if (parsedRecord == null || ! isToBeProcessed(parsedRecord)) {
				continue;
			}
			parsedRecord.setStep(step);
			parsedRecord.setOwner(parsedRecord.getModifiedBy());

			if (firstStepToBeProcessed) {
				CollectRecordSummary oldRecordSummary = findAlreadyExistingRecordSummary(parsedRecord);
				boolean newRecord = oldRecordSummary == null;
				if (newRecord) {
					insertRecordDataUntilStep(operations, parsedRecord, step);
				} else {
					// overwrite existing record
					lastRecordId = oldRecordSummary.getId();
					lastRecordOriginalStep = oldRecordSummary.getStep();
					parsedRecord.setId(lastRecordId);
					operations.setOriginalStep(lastRecordOriginalStep);
					operations.addUpdate(parsedRecord, step, step.after(lastRecordOriginalStep));
				}
				firstStepToBeProcessed = false;
			} else {
				parsedRecord.setId(lastRecordId);
				operations.addUpdate(parsedRecord, step, step.after(lastRecordOriginalStep));
			}
		}
		if (operations.hasMissingSteps()) {
			throw new MissingStepsException(operations);
		}
		return operations;
	}

	private boolean isToBeProcessed(CollectRecord record) {
		return includeRecordPredicate == null || includeRecordPredicate.evaluate(record);
	}

	private CollectRecordSummary findAlreadyExistingRecordSummary(
			CollectRecord parsedRecord) {
		return recordManager.loadUniqueRecordSummaryByKeys((CollectSurvey) parsedRecord.getSurvey(), 
				parsedRecord.getRootEntity().getName(), parsedRecord.getRootEntityKeyValues());
	}

	private void insertRecordDataUntilStep(RecordOperations operations,
			CollectRecord record, Step step) {
		List<Step> previousSteps = new ArrayList<Step>();
		for (Step s : Step.values()) {
			if (s.beforeEqual(step)) {
				previousSteps.add(s);
			}
		}
		for (Step previousStep : previousSteps) {
			record.setStep(previousStep);
			switch (previousStep) {
			case ENTRY:
				operations.addInsert(record);
				break;
			default:
				operations.addUpdate(record, previousStep, true);
			}
		}
	}

}