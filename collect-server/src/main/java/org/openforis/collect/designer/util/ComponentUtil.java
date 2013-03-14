package org.openforis.collect.designer.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.compress.utils.CharsetNames;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;

/**
 * 
 * @author S. Ricci
 *
 */
public class ComponentUtil {
	
	public static final String COMPOSER_ID = "$composer";

	@SuppressWarnings("unchecked")
	public static <T> T getComposer(Component view)  {
		return (T) view.getAttribute(COMPOSER_ID);
	}
	
	public static void addClass(HtmlBasedComponent component, String className) {
		String oldSclass = component.getSclass();
		if ( oldSclass == null ) {
			oldSclass = "";
		}
		if ( !  oldSclass.contains(className) ) {
			component.setSclass(oldSclass + " " + className);
		}
	}
	
	public static void removeClass(HtmlBasedComponent component, String className) {
		String oldSclass = component.getSclass();
		if ( oldSclass != null ) {
			component.setSclass(oldSclass.replaceAll(className, ""));
		}
	}

	public static void toggleClass(HtmlBasedComponent component, String className, boolean present) {
		if ( present ) {
			addClass(component, className);
		} else {
			removeClass(component, className);
		}
	}

	public static String createUrl(String base, Map<String, String> queryParams) {
		List<BasicNameValuePair> convertedParams = new ArrayList<BasicNameValuePair>();
		Set<Entry<String, String>> paramsEntrySet = queryParams.entrySet();
		for (Entry<String, String> param : paramsEntrySet) {
			BasicNameValuePair valuePair = new BasicNameValuePair(param.getKey(), param.getValue());
			convertedParams.add(valuePair);
		}
		String queryString = URLEncodedUtils.format(convertedParams, CharsetNames.UTF_8);
		String result = base + "?" + queryString;
		return result;
	}
}
