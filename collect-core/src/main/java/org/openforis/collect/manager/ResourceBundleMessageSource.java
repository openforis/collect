package org.openforis.collect.manager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
public class ResourceBundleMessageSource extends AbstractMessageSource {

	private static final String VALIDATION_BUNDLE_NAME = "org/openforis/collect/resourcebundles/validation";
	
	private Locale currentLocale;
	private List<String> bundleBaseNames;
	private Map<Locale, List<PropertyResourceBundle>> localeToResourceBundles;
	
	public ResourceBundleMessageSource() {
		this(Arrays.asList(VALIDATION_BUNDLE_NAME));
	}
	
	public ResourceBundleMessageSource(List<String> bundleBaseNames) {
		super();
		this.bundleBaseNames = bundleBaseNames;
		this.localeToResourceBundles = new HashMap<Locale, List<PropertyResourceBundle>>();
	}

	@Override
	public String getMessage(Locale locale, String code, Object... args) {
		List<PropertyResourceBundle> resourceBundles = getBundles(locale);
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

	protected List<PropertyResourceBundle> getBundles(Locale locale) {
		List<PropertyResourceBundle> bundles = localeToResourceBundles.get(locale);
		if ( bundles == null ) {
			bundles = initBundles(locale);
		}
		return bundles;
	}
	
	protected List<PropertyResourceBundle> initBundles(Locale locale) {
		List<PropertyResourceBundle> resourceBundles = new ArrayList<PropertyResourceBundle>();
		for (String baseName : bundleBaseNames) {
			PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle(baseName, locale);
			resourceBundles.add(bundle);
		}
		localeToResourceBundles.put(locale, resourceBundles);
		return resourceBundles;
	}
	
	public void setBundleBaseNames(List<String> bundleBaseNames) {
		this.bundleBaseNames = bundleBaseNames;
	}
	
	@Override
	public Locale getCurrentLocale() {
		return currentLocale;
	}
	
	@Override
	public void setCurrentLocale(Locale locale) {
		this.currentLocale = locale;
	}
	/*
	public static void main(String[] args) {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource(Arrays.asList("org/openforis/collect/resourcebundles/validation"));
		System.out.println(messageSource.getMessage("validation.specifiedError"));
	}
	*/
}
