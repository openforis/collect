package org.openforis.collect.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 * 
 * @author S. Ricci
 *
 */
public class MessageBundleHelper {
	
	@Autowired
	private MessageSource messageSource;

	public String getMessage(String code, Object[] args, Locale locale)
			throws NoSuchMessageException {
		return getMessage(code, args, code, locale);
	}
	
	public String getMessage(String code) {
		return getMessage(code, null, code, Locale.ENGLISH);
	}
	
	public String getMessage(String code, Object[] args) {
		return getMessage(code, args, code, Locale.ENGLISH);
	}
	
	public String getMessage(String code, Locale locale) {
		return getMessage(code, null, code, locale);
	}

	public String getMessage(String code, Object[] args, String defaultMessage,
			Locale locale) {
		return messageSource.getMessage(code, args, defaultMessage, locale);
	}

}
