package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "org.openforis.collect.io.metadata.collectearth.balloon.messages"; //$NON-NLS-1$

	private Messages() {
	}

	public static String getString(String key, String language) {
		try {
			ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale(language) );
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
