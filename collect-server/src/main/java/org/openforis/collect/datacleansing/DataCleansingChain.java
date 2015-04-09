package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.openforis.idm.metamodel.Survey;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingChain extends PersistedSurveyObject {

	private static final long serialVersionUID = 1L;
	
	private List<DataCleansingStep> steps;
	
	public DataCleansingChain(Survey survey) {
		super(survey);
		steps = new ArrayList<DataCleansingStep>();
	}
	
	public void addStep(DataCleansingStep step) {
		steps.add(step);
	}
	
	public List<DataCleansingStep> getSteps() {
		return steps;
	}
	
}
