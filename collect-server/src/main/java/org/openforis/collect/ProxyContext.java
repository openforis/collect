package org.openforis.collect;

import java.util.Locale;

import org.openforis.collect.manager.MessageSource;
import org.openforis.idm.metamodel.SurveyContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class ProxyContext {

	private Locale locale;
	private MessageSource messageSource;
	private SurveyContext surveyContext;
	
	public ProxyContext(Locale locale, MessageSource messageSource, SurveyContext surveyContext) {
		super();
		this.locale = locale;
		this.messageSource = messageSource;
		this.surveyContext = surveyContext;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public MessageSource getMessageSource() {
		return messageSource;
	}

	public SurveyContext getSurveyContext() {
		return surveyContext;
	}
	
}
