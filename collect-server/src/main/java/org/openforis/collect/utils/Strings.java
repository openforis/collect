package org.openforis.collect.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class Strings {

	public static String htmlToText(String text) {
		String result = HtmlUtils.htmlUnescape(text);
		result = result.replaceAll("<br>", "\n");
		return result;
	}

	public static String[] filterNotBlank(String... texts) {
		List<String> result = filterNotBlank(Arrays.asList(texts));
		return result.toArray(new String[result.size()]);
	}

	private static List<String> filterNotBlank(List<String> list) {
		List<String> result = new ArrayList<String>(list);
		CollectionUtils.filter(result, new Predicate() {
			@Override
			public boolean evaluate(Object text) {
				return StringUtils.isNotBlank((String) text);
			}
		});
		return result;
	}

}
