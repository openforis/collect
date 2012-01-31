/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.RealRange;

/**
 * @author S. Ricci
 *
 */
public class RealRangeProxy implements Proxy {

	private transient RealRange realRange;

	public RealRangeProxy(RealRange realRange) {
		super();
		this.realRange = realRange;
	}

	@ExternalizedProperty
	public Double getFrom() {
		return realRange.getFrom();
	}

	@ExternalizedProperty
	public Double getTo() {
		return realRange.getTo();
	}
	
}
