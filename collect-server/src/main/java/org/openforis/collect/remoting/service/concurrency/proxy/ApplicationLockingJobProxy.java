/**
 * 
 */
package org.openforis.collect.remoting.service.concurrency.proxy;

import org.openforis.collect.concurrency.ApplicationLockingJob;
import org.openforis.concurrency.proxy.JobProxy;

/**
 * @author Ste
 *
 */
public class ApplicationLockingJobProxy extends JobProxy {

	public ApplicationLockingJobProxy(ApplicationLockingJob job) {
		super(job);
	}

}
