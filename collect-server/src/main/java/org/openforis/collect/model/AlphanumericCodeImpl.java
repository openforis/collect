/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.model.AlphanumericCode;

/**
 * @author M. Togna
 * 
 */
public class AlphanumericCodeImpl extends AbstractCode<String> implements AlphanumericCode {

	public AlphanumericCodeImpl(String value, String qualifier) {
		super(value, qualifier);
	}

	public AlphanumericCodeImpl(String stringValue) {
		super(stringValue);
	}

	// @Override
	// protected boolean isValid() {
	// return !this.isBlank();
	// }

	@Override
	public String getCode() {
		return this.getText1();
	}

	@Override
	public boolean isFormatValid() {
		return false;
	}

}
