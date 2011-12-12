/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import java.util.regex.Pattern;

import org.openforis.idm.model.IntegerRange;

/**
 * @author M. Togna
 * 
 */
public class IntegerRangeImpl extends AbstractNumericRange<Integer> implements IntegerRange {
	
	private static final String REGEX = "([0-9]+)-([0-9]+)";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public IntegerRangeImpl(String stringValue) {
		super(stringValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.NumericRange#getFrom()
	 */
	@Override
	public Integer getFrom() {
		try {
			return Integer.parseInt(getText1());
		} catch (NumberFormatException e) {
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.NumericRange#getTo()
	 */
	@Override
	public Integer getTo() {
		try {
			return Integer.parseInt(getText2());
		} catch (NumberFormatException e) {
		}
		return null;

	}

	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}

}
