package org.openforis.collect.remoting.service.dataExport;

import org.openforis.collect.remoting.service.dataProcessing.DataProcessingState;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportState extends DataProcessingState {

	public enum Format {
		XML, CSV
	}
	
	private Format format;

	public DataExportState(Format format) {
		super();
		this.format = format;
	}
	
	public Format getFormat() {
		return format;
	}

}
