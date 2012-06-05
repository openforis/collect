/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.Date;

/**
 * @author S. Ricci
 *
 */
public class DateProxy implements Proxy {

	private transient Date date;

	public DateProxy(Date date) {
		super();
		this.date = date;
	}

	@ExternalizedProperty
	public Integer getDay() {
		return date.getDay();
	}

	@ExternalizedProperty
	public Integer getMonth() {
		return date.getMonth();
	}

	@ExternalizedProperty
	public Integer getYear() {
		return date.getYear();
	}
	
	
}
