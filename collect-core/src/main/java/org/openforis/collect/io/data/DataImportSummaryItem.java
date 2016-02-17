package org.openforis.collect.io.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummaryItem {

	private int entryId;
	private CollectRecordSummary record;
	private CollectRecordSummary conflictingRecord;
	private List<Step> steps;
	
	/**
	 * Map containing the warnings for each step (if any).
	 * The warnings are stored in a Map where the node path is the key of the warning.
	 */
	private Map<Step, List<NodeUnmarshallingError>> warnings;

	public DataImportSummaryItem(int entryId, CollectRecordSummary record, List<Step> steps) {
		this.entryId = entryId;
		this.record = record;
		this.steps = steps;
		this.warnings = new HashMap<CollectRecord.Step, List<NodeUnmarshallingError>>();
	}
	
	public DataImportSummaryItem(int entryId, CollectRecordSummary record, List<Step> steps, CollectRecordSummary conflictingRecord) {
		this(entryId, record, steps);
		this.conflictingRecord = conflictingRecord;
	}

	public int getEntryId() {
		return entryId;
	}

	public void setEntryId(int entryId) {
		this.entryId = entryId;
	}

	public CollectRecordSummary getRecord() {
		return record;
	}

	public void setRecord(CollectRecordSummary record) {
		this.record = record;
	}

	public CollectRecordSummary getConflictingRecord() {
		return conflictingRecord;
	}

	public void setConflictingRecord(CollectRecordSummary conflictingRecord) {
		this.conflictingRecord = conflictingRecord;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	public Map<Step, List<NodeUnmarshallingError>> getWarnings() {
		return warnings;
	}

	public void setWarnings(Map<Step, List<NodeUnmarshallingError>> warnings) {
		this.warnings = warnings;
	}
	
	public int getRecordCompletionPercent() {
		return record.getCompletionPercent();
	}

	public int getConflictingRecordCompletionPercent() {
		return conflictingRecord == null ? -1 : conflictingRecord.getCompletionPercent();
	}

}
