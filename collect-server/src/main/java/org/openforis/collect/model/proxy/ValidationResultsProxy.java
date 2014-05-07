/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;
import java.util.Locale;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.manager.MessageSource;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class ValidationResultsProxy implements Proxy {

	private transient Attribute<?, ?> attribute;
	private transient ValidationResults validationResults;
	private transient MessageSource messageSource;
	private Locale locale;

	public ValidationResultsProxy(MessageSource messageSource, Locale locale, Attribute<?, ?> attribute, ValidationResults validationResults) {
		this.messageSource = messageSource;
		this.attribute = attribute;
		this.validationResults = validationResults;
		this.locale = locale;
	}

	@ExternalizedProperty
	public List<ValidationResultProxy> getErrors() {
		return ValidationResultProxy.fromList(messageSource, locale, attribute, validationResults.getErrors());
	}

	@ExternalizedProperty
	public List<ValidationResultProxy> getWarnings() {
		return ValidationResultProxy.fromList(messageSource, locale, attribute, validationResults.getWarnings());
	}

}
