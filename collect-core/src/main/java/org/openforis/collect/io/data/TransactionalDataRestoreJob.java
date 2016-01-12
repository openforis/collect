package org.openforis.collect.io.data;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Component(value=TransactionalDataRestoreJob.JOB_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TransactionalDataRestoreJob extends DataRestoreJob {
	
	public static final String JOB_NAME = "transactionalDataRestoreJob";

	@Override
	@Transactional(rollbackFor=RestoreException.class)
	public synchronized void run() {
		super.run();
		if (! isCompleted()) {
			throw new RestoreException();
		}
	}

	private class RestoreException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
