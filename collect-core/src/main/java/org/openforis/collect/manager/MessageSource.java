package org.openforis.collect.manager;

import java.util.Locale;

/**
 * 
 * @author S. Ricci
 *
 */
public interface MessageSource {

	String getMessage(String code, Object... args);
	
	String getMessage(Locale locale, String code, Object... args);
	
	Locale getCurrentLocale();
	
	void setCurrentLocale(Locale locale);
	
}
