package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class AttributeEvent extends RecordEvent {
	
	private ValidationResultsView validationResults;

	public AttributeEvent(String surveyName, Integer recordId, RecordStep step,
			String definitionId, List<String> ancestorIds, String nodeId,
			Date timestamp, String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId, timestamp, userName);
	}

	public ValidationResultsView getValidationResults() {
		return validationResults;
	}
	
	public void setValidationResults(ValidationResultsView validationResults) {
		this.validationResults = validationResults;
	}
}
