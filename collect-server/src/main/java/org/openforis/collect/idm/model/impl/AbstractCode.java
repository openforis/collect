/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.model.Code;

/**
 * @author M. Togna
 * 
 */
public abstract class AbstractCode<T> extends AbstractValue implements Code<T> {

	private String qualifier;

	public AbstractCode(String value, String qualifier) {
		this(value);
		this.qualifier = qualifier;
	}

	public AbstractCode(String stringValue) {
		super(stringValue);
	}

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

}
