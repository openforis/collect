package org.openforis.collect.manager.process;

import static org.openforis.collect.manager.process.ProcessStatus.Step.*;

/**
 * 
 * @author S. Ricci
 *
 */
public class ProcessStatus {

	public enum Step {
		INIT, RUN, COMPLETE, CANCEL, ERROR;
	}
	
	private Step step;
	private long total;
	private long processed;
	private String errorMessage;

	public ProcessStatus() {
		init();
	}

	protected void init() {
		step = INIT;
		total = -1;
		processed = -1;
	}
	
	public void incrementProcessed() {
		processed ++;
	}
	
	public void start() {
		step = RUN;
		processed = 0;
	}

	public void cancel() {
		step = CANCEL;
	}

	public void complete() {
		step = COMPLETE;
	}

	public void error() {
		step = ERROR;			
	}
	
	public boolean isInit() {
		return step == INIT;
	}

	public boolean isRunning() {
		return step == RUN;
	}
	
	public boolean isComplete() {
		return step == COMPLETE;
	}
	
	public boolean isError() {
		return step == ERROR;
	}

	public Step getStep() {
		return step;
	}
	
	public long getTotal() {
		return total;
	}
	
	public void setTotal(long value) {
		total = value;
	}

	public long getProcessed() {
		return processed;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}
