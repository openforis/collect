/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.Proxy;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.model.Attribute;

/**
 * @author M. Togna
 * 
 */
public class ValidationResultProxy implements Proxy {

	private String validationMessage;

	public ValidationResultProxy(MessageSource messageSource, Locale locale, Attribute<?, ?> attribute, ValidationResult validationResult) {
		ValidationMessageBuilder validationMessageBuilder = ValidationMessageBuilder.createInstance(messageSource);
		this.validationMessage = validationMessageBuilder.getValidationMessage(attribute, validationResult, locale);
	}

	public static List<ValidationResultProxy> fromList(MessageSource messageSource, Locale locale, Attribute<?, ?> attribute, List<ValidationResult> list) {
		if (list == null) {
			return Collections.emptyList();
		} else {
			List<ValidationResultProxy> proxies = new ArrayList<ValidationResultProxy>();
			for (ValidationResult validationResults : list) {
				proxies.add(new ValidationResultProxy(messageSource, locale, attribute, validationResults));
			}
			return proxies;
		}
	}

	public String getValidationMessage() {
		return validationMessage;
	}

}
