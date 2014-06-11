package org.openforis.collect.manager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Gets messages from resource bundles.
 * 
 * 
 * @author S. Ricci
 *
 */
public class ResourceBundleMessageSource implements MessageSource {

	private static final String VALIDATION_BUNDLE_NAME = "org/openforis/collect/resourcebundles/validation";
	
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
					try {
						String messageKey = resourceBundle.getString(code);
						String result = MessageFormat.format(messageKey, args);
						return result;
					} catch (Exception e) {
						//resource not found
					}
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
			PropertyResourceBundle bundle = findBundle(locale, baseName);
			if ( bundle != null ) {
				resourceBundles.add(bundle);
			}
		}
		localeToResourceBundles.put(locale, resourceBundles);
		return resourceBundles;
	}

	protected PropertyResourceBundle findBundle(Locale locale, String baseName) {
		PropertyResourceBundle bundle = null;
		try {
			bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle(baseName, locale, new ResourceBundle.Control() {
				@Override
				public Locale getFallbackLocale(String baseName, Locale locale) {
					return null;
				}
			});
			return bundle;
		} catch ( Exception e ) {
			//missing resource exception
			return null;
		}
	}
	
	public void setBundleBaseNames(List<String> bundleBaseNames) {
		this.bundleBaseNames = bundleBaseNames;
	}
	
}
