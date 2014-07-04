package org.openforis.collect.io.data.proxy;

import java.io.File;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.io.data.CSVDataExportProcess;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportProcessProxy implements Proxy {

	private transient CSVDataExportProcess process;

	public DataExportProcessProxy(CSVDataExportProcess process) {
		this.process = process;
	}

	@ExternalizedProperty
	public DataExportStatusProxy getStatus() {
		return new DataExportStatusProxy(process.getStatus());
	}
	
	@ExternalizedProperty
	public String getOutputFileName() {
		File outputFile = process.getOutputFile();
		return outputFile == null ? null: outputFile.getAbsolutePath();
	}

}