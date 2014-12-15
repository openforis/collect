/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.IntegerRange;

/**
 * @author S. Ricci
 *
 */
public class IntegerRangeProxy implements Proxy {

	private transient IntegerRange integerRange;

	public IntegerRangeProxy(IntegerRange integerRange) {
		super();
		this.integerRange = integerRange;
	}

	@ExternalizedProperty
	public Integer getFrom() {
		return integerRange.getFrom();
	}

	@ExternalizedProperty
	public Integer getTo() {
		return integerRange.getTo();
	}
	
}
