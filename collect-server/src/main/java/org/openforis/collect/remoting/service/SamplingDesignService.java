/**
 * 
 */
package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.model.proxy.SamplingDesignSummariesProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

/**
 * @author S. Ricci
 */
public class SamplingDesignService {

	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	@Secured("ROLE_ADMIN")
	public SamplingDesignSummariesProxy loadBySurvey(int surveyId, int offset, int maxRecords) {
		return loadBySurvey(false, surveyId, offset, maxRecords);
	}

	@Secured("ROLE_ADMIN")
	public SamplingDesignSummariesProxy loadBySurveyWork(int surveyId, int offset, int maxRecords) {
		return loadBySurvey(true, surveyId, offset, maxRecords);
	}

	protected SamplingDesignSummariesProxy loadBySurvey(boolean work, int surveyId,
			int offset, int maxRecords) {
		SamplingDesignSummaries summaries;
		if (work) {
			summaries = samplingDesignManager.loadBySurveyWork(surveyId, offset, maxRecords);
		} else {
			summaries = samplingDesignManager.loadBySurvey(surveyId, offset, maxRecords);
		}
		return new SamplingDesignSummariesProxy(summaries);
	}

}
