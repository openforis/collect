/**
 * 
 */
package org.openforis.collect.spring;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.AbstractMessageSource;
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
public class SpringMessageSource extends AbstractMessageSource {

	private final Log log = LogFactory.getLog(SpringMessageSource.class);
	
	@Autowired
	private ApplicationContext context;
	
	@Override
	public String getMessage(Locale locale, String code, Object... args) {
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

	@Override
	public Locale getCurrentLocale() {
		Locale locale = LocaleContextHolder.getLocale();
		return locale;
	}
	
	@Override
	public void setCurrentLocale(Locale locale) {
		//managed by Spring
	}

}
