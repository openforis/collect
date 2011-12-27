package org.openforis.collect.model;

import java.util.HashMap;
import java.util.Map;

import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.DefaultRecord;
import org.openforis.idm.model.ModelObject;

/**
 * @author G. Miceli
 */
public class CollectRecord extends DefaultRecord {

	public enum Step {
		ENTRY, CLEANSING, ANALYSIS
	}

	private Step step;
	private boolean submitted;
	private Map<Long, ModelObject<? extends SchemaObjectDefinition>> modelObjectsMap;

	public CollectRecord(Survey survey, ModelVersion modelVersion) {
		super(survey, modelVersion);
		this.step = Step.ENTRY;
		this.submitted = false;
		this.modelObjectsMap = new HashMap<Long, ModelObject<? extends SchemaObjectDefinition>>();
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

	public ModelObject<? extends SchemaObjectDefinition> getModelObjectById(Long id) {
		return this.modelObjectsMap.get(id);
	}
}
