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

	public AbstractCode(String code, String qualifier) {
		this(code);
		setValue2(qualifier);
	}

	public AbstractCode(String code) {
		super(code);
	}

	@Override
	public String getQualifier() {
		return this.getValue2();
	}

}
