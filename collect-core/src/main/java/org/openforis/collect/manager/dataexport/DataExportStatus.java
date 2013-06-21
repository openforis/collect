package org.openforis.collect.manager.dataexport;

import org.openforis.collect.manager.process.ProcessStatus;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportStatus extends ProcessStatus {

	public enum Format {
		XML, CSV
	}
	
	private Format format;

	public DataExportStatus(Format format) {
		super();
		this.format = format;
	}
	
	public Format getFormat() {
		return format;
	}

}
