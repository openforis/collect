package org.openforis.collect.io.data.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.io.data.RecordImportError;

public class RecordImportErrorProxy implements Proxy {
	
	private transient RecordImportError error;

	public RecordImportErrorProxy(RecordImportError error) {
		this.error = error;
	}
	
	@ExternalizedProperty
	public String getLevel() {
		return error.getLevel().name();
	}

	@ExternalizedProperty
	public String getEntryName() {
		return error.getEntryName();
	}
	
	@ExternalizedProperty
	public String getErrorMessage() {
		return error.getErrorMessage();
	}
	
}
