package org.openforis.collect.io.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.persistence.xml.NodeUnmarshallingError;
import org.openforis.collect.utils.Dates;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportSummaryItem {

	private int entryId;
	private CollectRecordSummary recordSummary;
	private CollectRecordSummary conflictingRecordSummary;
	private List<Step> steps;
	
	/**
	 * Map containing the warnings for each step (if any).
	 * The warnings are stored in a Map where the node path is the key of the warning.
	 */
	private Map<Step, List<NodeUnmarshallingError>> warnings;

	public DataImportSummaryItem(int entryId, CollectRecordSummary recordSummary, List<Step> steps) {
		this.entryId = entryId;
		this.recordSummary = recordSummary;
		this.steps = steps;
		this.warnings = new HashMap<CollectRecord.Step, List<NodeUnmarshallingError>>();
	}
	
	public DataImportSummaryItem(int entryId, CollectRecordSummary record, List<Step> steps, CollectRecordSummary conflictingRecord) {
		this(entryId, record, steps);
		this.conflictingRecordSummary = conflictingRecord;
	}

	public int getEntryId() {
		return entryId;
	}

	public void setEntryId(int entryId) {
		this.entryId = entryId;
	}

	public CollectRecordSummary getRecordSummary() {
		return recordSummary;
	}

	public void setRecordSummary(CollectRecordSummary recordSummary) {
		this.recordSummary = recordSummary;
	}

	public CollectRecordSummary getConflictingRecordSummary() {
		return conflictingRecordSummary;
	}

	public void setConflictingRecordSummary(CollectRecordSummary conflictingRecordSummary) {
		this.conflictingRecordSummary = conflictingRecordSummary;
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
		return recordSummary.getCompletionPercent();
	}

	public int getRecordFilledAttributesCount() {
		return recordSummary == null ? 0 : recordSummary.getFilledAttributesCount();
	}
	
	public int getConflictingRecordCompletionPercent() {
		return conflictingRecordSummary == null ? -1 : conflictingRecordSummary.getCompletionPercent();
	}

	public int getConflictingRecordFilledAttributesCount() {
		return conflictingRecordSummary == null ? -1 : conflictingRecordSummary.getFilledAttributesCount();
	}
	
	public int calculateCompletionDifferencePercent() {
		if (conflictingRecordSummary == null || recordSummary.getFilledAttributesCount() == 0) {
			return 100;
		}
		double result = (double) (100 * 
					(recordSummary.getFilledAttributesCount() - conflictingRecordSummary.getFilledAttributesCount())
						/ conflictingRecordSummary.getFilledAttributesCount() );
		return Double.valueOf(Math.ceil(result)).intValue();
	}

	/**
	 * Level of "importability".
	 * -1 - there can be a problem: the record that is being imported is older, less complete or with more errors than the existing one
	 * 0 - the record being imported is the same as the existing one
	 * 1 - the record is new or contains more filled attributes than the existing one
	 * @return
	 */
	public int calculateImportabilityLevel() {
		if (conflictingRecordSummary == null) {
			//new
			return 1;
		} else {
			int modifiedDateCompare = Dates.compareUpToSecondsOnly(
					ObjectUtils.defaultIfNull(recordSummary.getModifiedDate(), recordSummary.getCreationDate()), 
					ObjectUtils.defaultIfNull(conflictingRecordSummary.getModifiedDate(), conflictingRecordSummary.getCreationDate()));
			if (modifiedDateCompare >= 0) {
				int differencePercent = calculateCompletionDifferencePercent();
				if (modifiedDateCompare == 0 && differencePercent == 0) {
					//same date and (probably) same data
					return 0;
				} else {
					if (differencePercent >= 0) {
						//newer and more complete
						return 1;
					} else {
						//newer but less complete
						return -1;
					}
				}
			} else {
				//older
				return -1;
			}
		} 
		
	}
	
}
