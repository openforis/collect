/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.metamodel.proxy.LanguageSpecificTextProxy;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationRule;
import org.openforis.idm.model.Attribute;

/**
 * @author M. Togna
 * 
 */
public class ValidationResultProxy implements Proxy {

	private transient ValidationResult validationResult;
	private String validationMessage;

	public ValidationResultProxy(MessageSource messageSource, Locale locale, Attribute<?, ?> attribute, ValidationResult validationResult) {
		this.validationMessage = createValidationMessage(messageSource, locale, attribute, validationResult);
		this.validationResult = validationResult;
	}

	public static List<ValidationResultProxy> fromList(MessageSource messageSource, Locale locale, Attribute<?, ?> attribute, List<ValidationResult> list) {
		if (list != null) {
			List<ValidationResultProxy> proxies = new ArrayList<ValidationResultProxy>();
			for (ValidationResult validationResults : list) {
				proxies.add(new ValidationResultProxy(messageSource, locale, attribute, validationResults));
			}
			return proxies;
		} else {
			return Collections.emptyList();
		}
	}

	@ExternalizedProperty
	public String getRuleName() {
		return validationResult.getValidator().getClass().getSimpleName();
	}
	
	public String getValidationMessage() {
		return validationMessage;
	}
	
	@ExternalizedProperty
	public List<LanguageSpecificTextProxy> getMessages() {
		ValidationRule<?> validator = validationResult.getValidator();
		if(validator instanceof Check<?>) {
			return LanguageSpecificTextProxy.fromList(((Check<?>) validator).getMessages());
		} else {
			return Collections.emptyList();
		}
	}
	
	protected String createValidationMessage(MessageSource messageSource, Locale locale, Attribute<?, ?> attribute,
			ValidationResult validationResult) {
		ValidationMessageBuilder validationMessageBuilder = ValidationMessageBuilder.createInstance(messageSource);
		return validationMessageBuilder.getValidationMessage(attribute, validationResult, locale);
	}

}
