package org.openforis.collect.remoting.service.dataexport;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.remoting.service.dataexport.DataExportState.Format;
import org.openforis.collect.remoting.service.dataprocessing.DataProcessingStateProxy;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportStateProxy extends DataProcessingStateProxy {

	public DataExportStateProxy(DataExportState state) {
		super(state);
	}

	@ExternalizedProperty
	public Format getFormat() {
		return ((DataExportState) state).getFormat();
	}
	
}
