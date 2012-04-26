package org.openforis.collect.remoting.service.export;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportState {

	private boolean exporting = false;
	private boolean compressing = false;
	private boolean error = false;
	private boolean cancelled = false;
	private boolean complete = false;

	private int count;
	private int total;

	@ExternalizedProperty
	public boolean isRunning() {
		return (! complete && ! error) && (exporting || compressing);
	}
	
	public boolean isExporting() {
		return exporting;
	}

	public void setExporting(boolean exporting) {
		this.exporting = exporting;
	}

	public boolean isCompressing() {
		return compressing;
	}

	public void setCompressing(boolean compressing) {
		this.compressing = compressing;
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

	public void setCount(int count) {
		this.count = count;
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
