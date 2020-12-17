package org.openforis.collect.manager.process.proxy;

import org.openforis.collect.Proxy;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.collect.manager.process.ProcessStatus.Step;

/**
 * 
 * @author S. Ricci
 *
 */
public class ProcessStatusProxy implements Proxy {

	protected transient ProcessStatus status;

	public ProcessStatusProxy(ProcessStatus status) {
		super();
		this.status = status;
	}

	public Step getStep() {
		return status.getStep();
	}

	public long getTotal() {
		return status.getTotal();
	}

	public long getProcessed() {
		return status.getProcessed();
	}

	public String getErrorMessage() {
		return status.getErrorMessage();
	}

	public boolean isInit() {
		return status.isInit();
	}

	public boolean isRunning() {
		return status.isRunning();
	}

	public boolean isComplete() {
		return status.isComplete();
	}

	public boolean isError() {
		return status.isError();
	}

}
