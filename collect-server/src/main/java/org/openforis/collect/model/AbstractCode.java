/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.model.Code;

/**
 * @author M. Togna
 * 
 */
public abstract class AbstractCode<T> extends AbstractValue implements Code<T> {

	public AbstractCode(String code, String qualifier) {
		this(code);
		this.setText2(qualifier);
	}

	public AbstractCode(String code) {
		super(code);
	}

	@Override
	public String getQualifier() {
		return this.getText2();
	}

	@Override
	public boolean isFormatValid() {
		return getCode() != null;
	}

}
