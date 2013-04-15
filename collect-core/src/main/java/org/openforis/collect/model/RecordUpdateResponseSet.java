/**
 * 
 */
package org.openforis.collect.model;

import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * @author S. Ricci
 *
 */
public class RecordUpdateResponseSet {

	private List<RecordUpdateResponse> responses;
	private Integer errors;
	private Integer skipped;
	private Integer missing;
	private Integer missingErrors;
	private Integer missingWarnings;
	private Integer warnings;
	
	public List<RecordUpdateResponse> getResponses() {
		return CollectionUtils.unmodifiableList(responses);
	}

	public void setResponses(List<RecordUpdateResponse> responses) {
		this.responses = responses;
	}

	public Integer getErrors() {
		return errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getMissing() {
		return missing;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getWarnings() {
		return warnings;
	}

	public void setWarnings(Integer warnings) {
		this.warnings = warnings;
	}

	public Integer getMissingErrors() {
		return missingErrors;
	}

	public void setMissingErrors(Integer missingErrors) {
		this.missingErrors = missingErrors;
	}

	public Integer getMissingWarnings() {
		return missingWarnings;
	}

	public void setMissingWarnings(Integer missingWarnings) {
		this.missingWarnings = missingWarnings;
	}

}
