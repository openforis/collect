package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class AttributeEvent extends RecordEvent {
	
	private ValidationResultsView validationResults;
	
	public ValidationResultsView getValidationResults() {
		return validationResults;
	}
	
	public void setValidationResults(ValidationResultsView validationResults) {
		this.validationResults = validationResults;
	}
}
