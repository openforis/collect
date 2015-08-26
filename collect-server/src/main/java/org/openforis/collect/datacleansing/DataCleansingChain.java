package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingChain extends PersistedSurveyObject {

	private static final long serialVersionUID = 1L;
	
	private String title;
	private String description;
	private List<DataCleansingStep> steps = new ArrayList<DataCleansingStep>();;
	
	public DataCleansingChain(CollectSurvey survey) {
		super(survey);
	}

	public DataCleansingChain(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}

	public void addStep(DataCleansingStep step) {
		steps.add(step);
	}
	
	public void removeStep(DataCleansingStep step) {
		steps.remove(step);
	}
	
	public void removeAllSteps() {
		steps.clear();
	}
	
	public List<DataCleansingStep> getSteps() {
		return steps;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
