package org.openforis.collect.utils;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class SurveyObjects {

	private static final String INTERNAL_NAME_INVALID_CHARACTERS_REGEX = "[^a-z0-9_]";
	
	public static String adjustInternalName(String name) {
		String result = StringUtils.trimToEmpty(name);
		result = result.toLowerCase(Locale.ENGLISH);
		result = result.replaceAll(INTERNAL_NAME_INVALID_CHARACTERS_REGEX, "_");
		return result;
	}
	
}
