package org.openforis.collect.io.data.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.io.data.DataExportStatus;
import org.openforis.collect.io.data.DataExportStatus.Format;
import org.openforis.collect.manager.process.proxy.ProcessStatusProxy;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportStatusProxy extends ProcessStatusProxy {

	public DataExportStatusProxy(DataExportStatus status) {
		super(status);
	}

	@ExternalizedProperty
	public Format getFormat() {
		return ((DataExportStatus) status).getFormat();
	}
}