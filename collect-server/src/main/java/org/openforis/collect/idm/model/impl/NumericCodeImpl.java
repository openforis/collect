/**
 * 
 */
package org.openforis.collect.idm.model.impl;

/**
 * @author M. Togna
 * 
 */
public class NumericCodeImpl extends AbstractCode<Integer> implements org.openforis.idm.model.NumericCode {

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
			code = Integer.parseInt(this.getStringValue());
		} catch (NumberFormatException e) {
			// code is not a valid integer.
		}
		return code;
	}

}
