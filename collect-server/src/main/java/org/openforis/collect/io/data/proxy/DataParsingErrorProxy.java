package org.openforis.collect.io.data.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.io.data.CSVDataImportJob.DataParsingError;
import org.openforis.collect.manager.referencedataimport.proxy.ParsingErrorProxy;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataParsingErrorProxy extends ParsingErrorProxy {

	private String fileName;

	public DataParsingErrorProxy(DataParsingError error) {
		super(error);
		this.fileName = error.getFileName();
	}
	
	@ExternalizedProperty
	public String getFileName() {
		return fileName;
	}
	
}
