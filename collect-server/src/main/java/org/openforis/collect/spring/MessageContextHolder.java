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
		String message = null;
		try {
			message = context.getMessage(code, args, locale);
		} catch ( NoSuchMessageException e ) {
			if ( log.isInfoEnabled() ) {
				log.info("Message with key '" + code + "' not found for locale '" + locale + "'");
			}
			message = context.getMessage(code, args, DEFAULT_LOCALE);
		}
		return message;
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
