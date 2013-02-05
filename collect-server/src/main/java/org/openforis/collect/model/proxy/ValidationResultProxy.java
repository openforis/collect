/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.metamodel.proxy.LanguageSpecificTextProxy;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.collect.util.ValidationMessageBuilder;
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

	public ValidationResultProxy(MessageContextHolder messageContextHolder, Attribute<?, ?> attribute, ValidationResult validationResult) {
		this.validationMessage = createValidationMessage(messageContextHolder, attribute, validationResult);
		this.validationResult = validationResult;
	}

	public static List<ValidationResultProxy> fromList(MessageContextHolder messageContextHolder, Attribute<?, ?> attribute, List<ValidationResult> list) {
		if (list != null) {
			List<ValidationResultProxy> proxies = new ArrayList<ValidationResultProxy>();
			for (ValidationResult validationResults : list) {
				proxies.add(new ValidationResultProxy(messageContextHolder, attribute, validationResults));
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
	
	protected String createValidationMessage(MessageContextHolder messageContextholder, Attribute<?, ?> attribute,
			ValidationResult validationResult) {
		ValidationMessageBuilder validationMessageBuilder = ValidationMessageBuilder.createInstance(messageContextholder);
		return validationMessageBuilder.getValidationMessage(attribute, validationResult);
	}

}
