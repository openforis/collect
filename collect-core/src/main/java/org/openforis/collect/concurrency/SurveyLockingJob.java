/**
 * 
 */
package org.openforis.collect.concurrency;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;

/**
 * @author S. Ricci
 *
 */
public abstract class SurveyLockingJob extends Job {

	protected CollectSurvey survey;
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

}
