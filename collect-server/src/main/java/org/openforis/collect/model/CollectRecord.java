package org.openforis.collect.model;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.impl.DefaultRecord;

/**
 * @author G. Miceli
 */
public class CollectRecord extends DefaultRecord {

	public enum Step {
		ENTRY, CLEANSING, ANALYSIS 
	}
	
	private Step step;
	private boolean submitted;
	
	public CollectRecord(Survey survey, ModelVersion version) {
		super(survey, version);
		this.step = Step.ENTRY;
		this.submitted = false;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}
	
	public boolean isSubmitted() {
		return submitted;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}
}
