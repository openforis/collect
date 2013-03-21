/**
 * 
 */
package org.openforis.collect.spring;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 * @author Mino Togna
 *
 */
@Component
public class MessageContextHolder {

	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	
	private final Log log = LogFactory.getLog(MessageContextHolder.class);
	
	@Autowired
	private ApplicationContext context;
	
	public String getMessage(String code, Object... args){
		Locale locale = getCurrentLocale();
		String message = getMessage(locale, code, args);
		if ( message == null ) {
			message = getMessage(DEFAULT_LOCALE, code, args);
		}
		return message;
	}

	public String getMessage(Locale locale, String code, Object[] args) {
		try {
			String message = context.getMessage(code, args, locale);
			return message;
		} catch ( NoSuchMessageException e ) {
			if ( log.isInfoEnabled() ) {
				log.info("Message with key '" + code + "' not found for locale '" + locale + "'");
			}
			return null;
		}
	}

	public Locale getCurrentLocale() {
		Locale locale = LocaleContextHolder.getLocale();
		return locale;
	}
	
	public String getCurrentLanguageCode() {
		Locale locale = getCurrentLocale();
		if ( locale == null ) {
			return null;
		} else {
			return locale.getLanguage();
		}
	}
	
}
