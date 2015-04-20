package org.openforis.collect.io.data;

import java.util.List;

import org.openforis.collect.model.CollectRecord.Step;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataBackupError {
	
	private int recordId;
	private Step recordStep;
	private List<String> recordKeys;
	private String errorMessage;

	public DataBackupError(int recordId, List<String> recordKeys, Step recordStep,
			String errorMessage) {
		super();
		this.recordId = recordId;
		this.recordKeys = recordKeys;
		this.recordStep = recordStep;
		this.errorMessage = errorMessage;
	}

	public String getJointRecordKeys() {
		return joinStringSkipNulls(recordKeys);
	}

	public String getRecordStepName() {
		return recordStep.name();
	}
	
	private String joinStringSkipNulls(List<String> values) {
		if (values == null) {
			return "";
		} else {
			String separator = ", ";
			StringBuilder sb = new StringBuilder();
			for (String v : values) {
				if (v != null) {
					if (sb.length() > 0) {
						sb.append(separator);
					}
					sb.append(v);
				}
			}
			return sb.toString();
		}
	}
	
	public int getRecordId() {
		return recordId;
	}

	public List<String> getRecordKeys() {
		return recordKeys;
	}

	public Step getRecordStep() {
		return recordStep;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public String toString() {
		return String.format("Error while backing up record with id %d keys %s step %s: %s", 
				recordId, recordKeys.toString(), recordStep.name(), errorMessage);

	}
	
}