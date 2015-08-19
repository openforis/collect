package org.openforis.collect.manager.process;

import java.io.Serializable;
import java.util.Observable;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataProcessingState extends Observable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean running = false;
	private boolean error = false;
	private boolean cancelled = false;
	private boolean complete = false;

	private int total;
	private int count;
	
	private String errorMessage;

	public DataProcessingState() {
		super();
	}
	
	public void reset() {
		count = 0;
		total = 0;
		running = false;
		complete = false;
		error = false;
	}
	
	public void incrementCount() {
		count++;
		setChanged();
		notifyObservers();
	}
	
	public void resetCount() {
		count = 0;
	}
	
	public boolean isRunning() {
		return (! complete && ! error) && running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public int getCount() {
		return count;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
		notifyObservers();
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
