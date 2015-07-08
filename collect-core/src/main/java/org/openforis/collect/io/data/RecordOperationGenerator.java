package org.openforis.collect.io.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordManager.RecordOperation;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.model.Entity;

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

	public List<RecordOperation> generate() throws IOException {
		List<RecordOperation> operations = new ArrayList<RecordOperation>();
		CollectRecord lastProcessedRecord = null;
		Step originalRecordStep = null;
		for (Step step : Step.values()) {
			CollectRecord parsedRecord = recordProvider.provideRecord(entryId,
					step);
			if (parsedRecord == null) {
				continue;
			}

			// record parsed successfully
			parsedRecord.setStep(step);

			if (lastProcessedRecord == null) {
				CollectRecord oldRecordSummary = findAlreadyExistingRecordSummary(parsedRecord);
				if (oldRecordSummary == null) {
					// insert new record
					// parsedRecord.setId(nextRecordId ++);
					operations.addAll(insertRecordDataUntilStep(parsedRecord,
							step));
				} else {
					// overwrite existing record
					originalRecordStep = oldRecordSummary.getStep();
					parsedRecord.setId(oldRecordSummary.getId());
					operations.add(RecordOperation.createUpdate(parsedRecord));
				}
				lastProcessedRecord = parsedRecord;
			} else {
				parsedRecord.setId(lastProcessedRecord.getId());
				// replaceData(parsedRecord, lastProcessedRecord);
				operations.add(RecordOperation.createUpdate(parsedRecord));
			}
			// if ( parseRecordResult.hasWarnings() ) {
			// addWarnings(entryName, parseRecordResult.getWarnings());
			// }
		}
		if (lastProcessedRecord != null) {
			// if the original record step is after the imported record one,
			// restore record step to the original one and revalidate the record
			// e.g. importing data from data entry step and the original record
			// was in analysis step
			if (originalRecordStep != null
					&& originalRecordStep.after(lastProcessedRecord.getStep())) {
				operations.add(restoreRecordStep(lastProcessedRecord,
						originalRecordStep));
			} else {
				// validate record and save the validation result
				validateRecord(lastProcessedRecord);
				operations.add(RecordOperation
						.createUpdate(lastProcessedRecord));
			}
		}
		return operations;
	}

	private CollectRecord findAlreadyExistingRecordSummary(
			CollectRecord parsedRecord) {
		CollectSurvey survey = (CollectSurvey) parsedRecord.getSurvey();
		List<String> keyValues = parsedRecord.getRootEntityKeyValues();
		Entity rootEntity = parsedRecord.getRootEntity();
		String rootEntityName = rootEntity.getName();
		List<CollectRecord> oldRecords = recordManager.loadSummaries(survey,
				rootEntityName, keyValues.toArray(new String[0]));
		if (oldRecords == null || oldRecords.isEmpty()) {
			return null;
		} else if (oldRecords.size() == 1) {
			return oldRecords.get(0);
		} else {
			throw new IllegalStateException(String.format(
					"Multiple records found in survey %s with key(s): %s",
					survey.getName(), keyValues));
		}
	}

	private List<RecordOperation> insertRecordDataUntilStep(
			CollectRecord record, Step step) {
		List<RecordOperation> operations = new ArrayList<RecordOperation>();

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
				operations.add(RecordOperation.createInsert(record));
				break;
			default:
				operations.add(RecordOperation.createUpdate(record));
			}
		}
		return operations;
	}

	private RecordOperation restoreRecordStep(CollectRecord record,
			Step originalRecordStep) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		CollectRecord originalRecord = recordManager.load(survey,
				record.getId(), originalRecordStep);
		originalRecord.setStep(originalRecordStep);
		validateRecord(originalRecord);
		return RecordOperation.createUpdate(originalRecord);
	}

	private void validateRecord(CollectRecord record) {
		try {
			recordManager.validate(record);
		} catch (Exception e) {
			// log().warn("Error validating record: " +
			// record.getRootEntityKeyValues(), e);
		}
	}
}