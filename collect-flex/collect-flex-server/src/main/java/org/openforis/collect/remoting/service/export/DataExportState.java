package org.openforis.collect.remoting.service.export;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportState {

	public enum Format {
		XML, CSV
	}
	
	private boolean running = false;
	private boolean error = false;
	private boolean cancelled = false;
	private boolean complete = false;
	private Format format;

	private int count;
	private int total;

	public DataExportState(Format format) {
		super();
		this.format = format;
	}
	
	@ExternalizedProperty
	public boolean isRunning() {
		return (! complete && ! error) && running;
	}
	
	public void reset() {
		count = 0;
		running = false;
		complete = false;
		error = false;
	}
	
	public void incrementCount() {
		count ++;
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
		return count;
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

	public Format getFormat() {
		return format;
	}

}
