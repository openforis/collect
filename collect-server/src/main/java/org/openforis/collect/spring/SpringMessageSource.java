/**
 * 
 */
package org.openforis.collect.spring;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.MessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 * @author Mino Togna
 *
 */
@Component
public class SpringMessageSource implements MessageSource {

	private final Log log = LogFactory.getLog(SpringMessageSource.class);
	
	@Autowired
	private ApplicationContext context;
	
	@Override
	public String getMessage(Locale locale, String code, Object... args) {
		try {
			String message = context.getMessage(code, args, locale);
			return message;
		} catch ( NoSuchMessageException e ) {
			if ( log.isDebugEnabled() ) {
				log.debug("Message with key '" + code + "' not found for locale '" + locale + "'");
			}
			return null;
		}
	}

}
