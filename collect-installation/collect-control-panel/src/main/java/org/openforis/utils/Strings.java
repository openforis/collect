package org.openforis.utils;

public abstract class Strings {

	public static boolean isBlank(String value) {
		return value == null || value.trim().length() == 0;
	}
}
