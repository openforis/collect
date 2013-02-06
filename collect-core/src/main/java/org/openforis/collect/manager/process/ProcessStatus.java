package org.openforis.collect.manager.process;

import static org.openforis.collect.manager.process.ProcessStatus.Step.*;

/**
 * 
 * @author S. Ricci
 *
 */
public class ProcessStatus {

	public enum Step {
		INIT, PREPARE, RUN, COMPLETE, CANCEL, ERROR;
	}
	
	private Step step;
	private int total;
	private int processed;
	private String errorMessage;

	public ProcessStatus() {
		step = INIT;
		total = -1;
		processed = -1;
	}
	
	public void incrementProcessed() {
		processed ++;
	}
	
	public void complete() {
		step = COMPLETE;
	}

	public void start() {
		step = RUN;
		processed = 0;
	}

	public void cancel() {
		step = CANCEL;
	}

	public void error() {
		step = ERROR;			
	}

	public boolean isRunning() {
		return step == RUN;
	}
	
	public boolean isComplete() {
		return step == COMPLETE;
	}
	
	public Step getStep() {
		return step;
	}
	
	public int getTotal() {
		return total;
	}
	
	public void setTotal(int value) {
		total = value;
	}

	public int getProcessed() {
		return processed;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}
