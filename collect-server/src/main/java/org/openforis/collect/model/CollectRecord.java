package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;

import org.openforis.idm.metamodel.ModelObjectDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.ModelObject;
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
	private Map<Long, ModelObject<? extends ModelObjectDefinition>> modelObjectsMap;

	public CollectRecord(Survey survey, ModelVersion version) {
		super(survey, version);
		this.step = Step.ENTRY;
		this.submitted = false;
		this.modelObjectsMap = new HashMap<Long, ModelObject<? extends ModelObjectDefinition>>();
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

	public ModelObject<? extends ModelObjectDefinition> getModelObjectById(Long id) {
		return this.modelObjectsMap.get(id);
	}
}
