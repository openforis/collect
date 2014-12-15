package org.openforis.collect.manager;

import java.util.Locale;

/**
 * 
 * @author S. Ricci
 *
 */
public interface MessageSource {

	/**
	 * Looks up for the message in the message source using the specified locale
	 * 
	 * @param locale
	 * @param code
	 * @param args
	 * @return
	 */
	String getMessage(Locale locale, String code, Object... args);
	
}
