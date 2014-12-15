/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.Code;

/**
 * @author S. Ricci
 *
 */
public class CodeProxy implements Proxy {

	private transient Code internalCode;

	public CodeProxy(Code code) {
		super();
		this.internalCode = code;
	}

	@ExternalizedProperty
	public String getCode() {
		return internalCode.getCode();
	}

	@ExternalizedProperty
	public String getQualifier() {
		return internalCode.getQualifier();
	}
	
	
}
