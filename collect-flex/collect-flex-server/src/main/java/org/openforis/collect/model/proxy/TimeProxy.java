/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.Time;

/**
 * @author S. Ricci
 *
 */
public class TimeProxy implements Proxy {

	private transient Time time;

	public TimeProxy(Time time) {
		super();
		this.time = time;
	}

	@ExternalizedProperty
	public Integer getHour() {
		return time.getHour();
	}

	@ExternalizedProperty
	public Integer getMinute() {
		return time.getMinute();
	}
	
	
}
