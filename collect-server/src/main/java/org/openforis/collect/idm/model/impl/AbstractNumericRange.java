/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openforis.idm.model.NumericRange;

/**
 * @author M. Togna
 * 
 */
public abstract class AbstractNumericRange<T extends Number> extends AbstractValue implements NumericRange<T> {

	private static final String REGEX = "[0-9]+-[0-9]+";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public AbstractNumericRange(String stringValue) {
		super(stringValue);
	}

	protected boolean isValidRange() {
		if (!this.isBlank()) {
			Matcher matcher = PATTERN.matcher(this.getValue1());
			return matcher.matches();
		}
		return false;
	}

}
