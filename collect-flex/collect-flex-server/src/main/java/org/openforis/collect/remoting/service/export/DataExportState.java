package org.openforis.collect.remoting.service.export;

import org.openforis.collect.remoting.service.dataImport.DataProcessingState;


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
