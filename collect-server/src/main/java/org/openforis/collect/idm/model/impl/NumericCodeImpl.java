/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.model.NumericCode;

/**
 * @author M. Togna
 * 
 */
public class NumericCodeImpl extends AbstractCode<Integer> implements NumericCode {

	public NumericCodeImpl(String value, String qualifier) {
		super(value, qualifier);
	}

	public NumericCodeImpl(String stringValue) {
		super(stringValue);
	}

	// @Override
	// protected boolean isValid() {
	// return false;
	// }

	@Override
	public Integer getCode() {
		Integer code = null;
		try {
			code = Integer.parseInt(this.getValue1());
		} catch (NumberFormatException e) {
		}
		return code;
	}

}
