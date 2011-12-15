/**
 * 
 */
package org.openforis.collect.model;

import java.util.regex.Pattern;

import org.openforis.idm.model.RealRange;

/**
 * @author M. Togna
 * 
 */
public class RealRangeImpl extends AbstractNumericRange<Double> implements RealRange {

	private static final String REGEX = "([0-9]+|[0-9]+\\.[0-9]+)-([0-9]+|[0-9]+\\.[0-9]+)";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public RealRangeImpl(String stringValue) {
		super(stringValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.NumericRange#getFrom()
	 */
	@Override
	public Double getFrom() {
		try {
			return Double.parseDouble(getText1());
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
	public Double getTo() {
		try {
			return Double.parseDouble(getText2());
		} catch (NumberFormatException e) {
		}
		return null;
	}

	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}

}
