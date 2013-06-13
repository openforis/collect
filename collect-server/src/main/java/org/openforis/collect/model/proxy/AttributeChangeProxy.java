package org.openforis.collect.model.proxy;

import java.util.Map;

import javax.servlet.ServletContext;

import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.spring.SpringMessageSource;
import org.openforis.idm.model.Attribute;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeChangeProxy extends NodeChangeProxy<AttributeChange> {

	public AttributeChangeProxy(AttributeChange change) {
		super(change);
	}

	@ExternalizedProperty
	public ValidationResultsProxy getValidationResults() {
		if ( change.getValidationResults() == null ) {
			return null;
		} else {
			MessageSource messageSource = getMessageSource();
			return new ValidationResultsProxy(messageSource, (Attribute<?, ?>) change.getNode(), change.getValidationResults());
		}
	}

	protected MessageSource getMessageSource() {
		return getContextBean(SpringMessageSource.class);
	}

	protected <T extends Object> T getContextBean(Class<T> type) {
		HttpGraniteContext graniteContext = (HttpGraniteContext) GraniteContext.getCurrentInstance();
		ServletContext servletContext = graniteContext.getServletContext();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
		T bean = applicationContext.getBean(type);
		return bean;
	}

	@ExternalizedProperty
	public Map<Integer, Object> getUpdatedFieldValues() {
		return change.getUpdatedFieldValues();
	}
	
}