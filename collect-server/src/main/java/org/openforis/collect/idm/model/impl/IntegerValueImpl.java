/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import org.openforis.idm.model.IntegerValue;

/**
 * @author M. Togna
 * 
 */
public class IntegerValueImpl extends AbstractNumericValue<Integer> implements IntegerValue {

	public IntegerValueImpl(String stringValue) {
		super(stringValue);
	}

	@Override
	public Integer getNumber() {
		try {
			return Integer.parseInt(this.getText1());
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
