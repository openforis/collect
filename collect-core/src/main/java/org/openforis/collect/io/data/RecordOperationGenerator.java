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

public class RecordOperationGenerator {
	private final RecordProvider recordProvider;
	private final RecordManager recordManager;
	private final int entryId;

	public RecordOperationGenerator(RecordProvider recordProvider,
			RecordManager recordManager, int entryId) {
		super();
		this.recordProvider = recordProvider;
		this.recordManager = recordManager;
		this.entryId = entryId;
	}

	public RecordOperations generate() throws IOException, MissingStepsException, RecordParsingException {
		RecordOperations operations = new RecordOperations();
		boolean firstStepToBeProcessed = true;
		Integer lastRecordId = null;
		for (Step step : Step.values()) {
			CollectRecord parsedRecord = recordProvider.provideRecord(entryId, step);
			if (parsedRecord == null) {
				continue;
			}
			parsedRecord.setStep(step);

			if (firstStepToBeProcessed) {
				CollectRecordSummary oldRecordSummary = findAlreadyExistingRecordSummary(parsedRecord);
				boolean newRecord = oldRecordSummary == null;
				if (newRecord) {
					insertRecordDataUntilStep(operations, parsedRecord, step);
				} else {
					// overwrite existing record
					lastRecordId = oldRecordSummary.getId();
					parsedRecord.setId(lastRecordId);
					operations.setOriginalStep(oldRecordSummary.getStep());
					operations.addUpdate(parsedRecord, step);
				}
				firstStepToBeProcessed = false;
			} else {
				parsedRecord.setId(lastRecordId);
				operations.addUpdate(parsedRecord, step);
			}
		}
		if (operations.hasMissingSteps()) {
			throw new MissingStepsException(operations);
		}
		return operations;
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
				operations.addInsert(record, previousStep);
				break;
			default:
				operations.addUpdate(record, previousStep);
			}
		}
	}

}