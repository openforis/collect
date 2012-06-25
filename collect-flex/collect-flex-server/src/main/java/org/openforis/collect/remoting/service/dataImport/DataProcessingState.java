package org.openforis.collect.remoting.service.dataImport;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataProcessingState {

	private boolean running = false;
	private boolean error = false;
	private boolean cancelled = false;
	private boolean complete = false;

	private int insertedCount;
	private int updatedCount;
	private int total;

	public DataProcessingState() {
		super();
	}
	
	@ExternalizedProperty
	public boolean isRunning() {
		return (! complete && ! error) && running;
	}
	
	public void reset() {
		insertedCount = 0;
		running = false;
		complete = false;
		error = false;
	}
	
	public void incrementInsertedCount() {
		insertedCount ++;
	}
	
	public void incrementUpdatedCount() {
		updatedCount ++;
	}
	
	public boolean isExporting() {
		return running;
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
		return insertedCount;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

}
