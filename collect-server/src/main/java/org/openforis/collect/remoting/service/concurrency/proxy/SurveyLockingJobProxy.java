/**
 * 
 */
package org.openforis.collect.remoting.service.concurrency.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.concurrency.proxy.JobProxy;

/**
 * @author Ste
 *
 */
public class SurveyLockingJobProxy extends JobProxy {

	public SurveyLockingJobProxy(SurveyLockingJob job) {
		super(job);
	}

	@ExternalizedProperty
	public int getSurveyId() {
		return ((SurveyLockingJob) job).getSurvey().getId();
	}
}
