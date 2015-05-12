/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.File;

/**
 * @author S. Ricci
 *
 */
public class FileProxy implements Proxy {

	private transient File file;

	public FileProxy(File file) {
		super();
		this.file = file;
	}

	@ExternalizedProperty
	public String getFilename() {
		return file.getFilename();
	}

	@ExternalizedProperty
	public Long getSize() {
		return file.getSize();
	}

}
