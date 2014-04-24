/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.model.Configuration;

/**
 * @author ste
 *
 */
public class ConfigurationProxy implements Proxy {

	private transient Configuration configuration;

	public ConfigurationProxy(Configuration configuration) {
		super();
		this.configuration = configuration;
	}
	
	@ExternalizedProperty
	public String getUploadPath() {
		return configuration.getUploadPath();
	}

	@ExternalizedProperty
	public String getDefaultUploadPath() {
		return null;
	}

	
	@ExternalizedProperty
	public String getIndexPath() {
		return configuration.getIndexPath();
	}
	
	@ExternalizedProperty
	public String getDefaultIndexPath() {
		return null;
	}

}
