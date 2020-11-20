package org.openforis.idm.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.Unit;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public final class IntegerRange extends NumericRange<Integer> {

	private static final String REGEX = "(-?\\d+)(-(-?\\d+))?";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	private static final String INTERNAL_STRING_FORMAT = "%d - %d";

	public IntegerRange(Integer value, Integer unitId) {
		super(value, value, unitId);
	}

	public IntegerRange(Integer from, Integer to, Integer unitId) {
		super(from, to, unitId);
	}

	public static IntegerRange parseIntegerRange(String string, Unit unit) {
		if ( StringUtils.isBlank(string) ) {
			return null;
		} else {
			Matcher matcher = PATTERN.matcher(string);
			if ( matcher.matches() ) {
				String fromStr = matcher.group(1);
				String toStr = matcher.group(3);
				if ( toStr == null ) {
					toStr = fromStr;
				}
				int from = Integer.parseInt(fromStr);
				int to = Integer.parseInt(toStr);
				return new IntegerRange(from, to, unit == null ? null : unit.getId());
			} else {
				return null;
			}
		}
	}
	
	@Override
	public String toPrettyFormatString() {
		return toInternalString();
	}
	
	@Override
	public String toInternalString() {
		return String.format(INTERNAL_STRING_FORMAT, getFrom(), getTo());
	}
	
}
