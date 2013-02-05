package org.openforis.collect.manager.process;

import static org.openforis.collect.manager.process.ProcessStatus.Step.*;

/**
 * 
 * @author S. Ricci
 *
 */
public class ProcessStatus {

	public enum Step {
		INITED, PREPARING, RUNNING, COMPLETE, CANCELLED, ERROR;
	}
	
	private Step step;

	public ProcessStatus() {
		step = INITED;
	}
	
	public void complete() {
		step = COMPLETE;
	}

	public void start() {
		step = RUNNING;
	}

	public void cancel() {
		step = CANCELLED;
	}

	public void error() {
		step = ERROR;			
	}

	public boolean isRunning() {
		return step == RUNNING;
	}
	
	public boolean isComplete() {
		return step == COMPLETE;
	}
	
	public Step getStep() {
		return step;
	}
	

}
