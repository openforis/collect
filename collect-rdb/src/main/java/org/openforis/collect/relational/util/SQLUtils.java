package org.openforis.collect.relational.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author S. Ricci
 *
 */
public class SQLUtils {

	public static List<String> doubleQuote(List<String> texts) {
		List<String> result = new ArrayList<String>(texts.size());
		for (String text : texts) {
			result.add(doubleQuote(text));
		}
		return result;
	}
	
	public static String doubleQuote(String text) {
		return "\"" + text + "\"";
	}

	public static String quote(String text) {
		return "'" + text + "'";
	}
}
