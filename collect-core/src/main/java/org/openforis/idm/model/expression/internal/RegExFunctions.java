package org.openforis.idm.model.expression.internal;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.COMMENTS;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.UNICODE_CASE;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
import static java.util.regex.Pattern.UNIX_LINES;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.ExpressionContext;

/**
 * @author S. Ricci
 */
public class RegExFunctions extends CustomFunctions {

	private static PatternCache PATTERN_CACHE = new PatternCache();
	
	public RegExFunctions(String namespace) {
		super(namespace);
		
		register("test", new CustomFunction(2) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return test((String) objects[0], (String) objects[1]);
			}
		});

		register("test", new CustomFunction(3) {
			public Object invoke(ExpressionContext expressionContext, Object[] objects) {
				return test((String) objects[0], (String) objects[1], (String) objects[2]);
			}
		});
}

	private static boolean test(String value, String regEx) {
		return test(value, regEx, null);
	}

	private static boolean test(String value, String regEx, String flags) {
		if (value == null) {
			return false;
		}
		if (regEx == null) {
			return true;
		}
		Pattern pattern = getOrCreatePattern(regEx, flags);
		Matcher matcher = pattern.matcher(value);
		boolean result = matcher.matches();
		return result;
	}

	private static Pattern getOrCreatePattern(String regEx, String flags) {
		String patternKey = getPatternKey(regEx, flags);
		Pattern pattern = PATTERN_CACHE.get(patternKey);
		if (pattern == null) {
			int flagsInt = toFlagsInt(flags);
			pattern = flagsInt > 0 ? Pattern.compile(regEx, flagsInt): Pattern.compile(regEx);
			PATTERN_CACHE.put(patternKey, pattern);
		}
		return pattern;
	}

	private static int toFlagsInt(String flags) {
		int flagsInt = 0;
		if (flags != null && flags.length() > 0) {
			char[] flagChars = flags.toCharArray();
			for (char flagChar : flagChars) {
				flagsInt += getFlagIntValue(flagChar);
			}
		}
		return flagsInt;
	}

	private static int getFlagIntValue(char flagChar) {
		switch (flagChar) {
		case 'd':
			return UNIX_LINES;
		case 'i':
			return CASE_INSENSITIVE;
		case 'x':
			return COMMENTS;
		case 'm':
			return MULTILINE;
		case 's':
			return DOTALL;
		case 'u':
			return UNICODE_CASE;
		case 'U':
			return UNICODE_CHARACTER_CLASS; 
		default:
			throw new IllegalArgumentException("Unsupported flag char: " + flagChar);
		}
	}
	
	private static String getPatternKey(String regEx, String flags) {
		return regEx + "|||" + flags;
	}
	
	private static class PatternCache extends LinkedHashMap<String, Pattern> {

		private static final long serialVersionUID = 1L;

		private static final int MAX_ENTRIES = 1000;

		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<String, Pattern> eldest) {
			return size() > MAX_ENTRIES;
		}

	}
}
