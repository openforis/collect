package org.openforis.collect.web.session;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportState {

	private boolean extracting = false;
	private boolean compressing = false;
	private boolean error = false;
	private boolean cancelled = false;

	private int count;
	private int total;

	public boolean isExtracting() {
		return extracting;
	}

	public void setExtracting(boolean extracting) {
		this.extracting = extracting;
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

}
