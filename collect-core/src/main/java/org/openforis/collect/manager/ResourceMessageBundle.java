package org.openforis.collect.manager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;

/**
 * Gets messages from resource bundles.
 * 
 * 
 * @author S. Ricci
 *
 */
public class ResourceMessageBundle extends AbstractMessageSource {

	private Locale locale;
	private List<String> bundleBaseNames;
	private Map<Locale, List<PropertyResourceBundle>> localeToResourceBundles;
	
	public ResourceMessageBundle(List<String> bundleBaseNames) {
		super();
		this.bundleBaseNames = bundleBaseNames;
		this.localeToResourceBundles = new HashMap<Locale, List<PropertyResourceBundle>>();
	}

	@Override
	public String getMessage(Locale locale, String code, Object... args) {
		List<PropertyResourceBundle> resourceBundles = localeToResourceBundles.get(locale);
		if ( resourceBundles == null || resourceBundles.isEmpty() ) {
			return null;
		} else {
			for (PropertyResourceBundle resourceBundle : resourceBundles) {
				if ( resourceBundle.containsKey(code) ) {
					String messageKey = resourceBundle.getString(code);
					String result = MessageFormat.format(messageKey, args);
					return result;
				}
			}
			return null;
		}
	}
	
	protected void initBundles(Locale locale) {
		List<PropertyResourceBundle> resourceBundles = new ArrayList<PropertyResourceBundle>();
		for (String baseName : bundleBaseNames) {
			PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle(baseName, locale);
			resourceBundles.add(bundle);
		}
		localeToResourceBundles.put(locale, resourceBundles);
	}
	
	public void setBundleBaseNames(List<String> bundleBaseNames) {
		this.bundleBaseNames = bundleBaseNames;
	}
	
	@Override
	public Locale getCurrentLocale() {
		return locale;
	}
	
	public void setCurrentLocale(Locale locale) {
		this.locale = locale;
		if ( ! localeToResourceBundles.containsKey(locale) ) {
			initBundles(locale);
		}
	}
	

}
