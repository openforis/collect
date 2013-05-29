package org.openforis.collect.manager;

import java.util.Locale;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AbstractMessageSource implements MessageSource {
	
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	
	@Override
	public String getMessage(String code, Object... args){
		Locale locale = getCurrentLocale();
		String message = getMessage(locale, code, args);
		if ( message == null ) {
			message = getMessage(DEFAULT_LOCALE, code, args);
		}
		return message;
	}
	
}
