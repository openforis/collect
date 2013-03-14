/**
 * 
 */
package org.openforis.collect.spring;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 * @author Mino Togna
 *
 */
@Component
public class MessageContextHolder {

	@Autowired
	private ApplicationContext context;
	
	public String getMessage(String code, Object... args){
		Locale locale = LocaleContextHolder.getLocale();
		return context.getMessage(code, args, locale);
	}
	
}
