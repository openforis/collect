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
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationRule;

/**
 * @author M. Togna
 * 
 */
public class ValidationResultProxy implements Proxy {

	private transient ValidationResult validationResult;

	public ValidationResultProxy(ValidationResult validationResult) {
		this.validationResult = validationResult;
	}

	public static List<ValidationResultProxy> fromList(List<ValidationResult> list) {
		if (list != null) {
			List<ValidationResultProxy> proxies = new ArrayList<ValidationResultProxy>();
			for (ValidationResult validationResults : list) {
				proxies.add(new ValidationResultProxy(validationResults));
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
	
	@ExternalizedProperty
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<LanguageSpecificTextProxy> getMessages() {
		ValidationRule<?> v = validationResult.getValidator();
		if ( v instanceof Check ) {
			List<LanguageSpecificText> m = ((Check) v).getMessages();
			List<LanguageSpecificTextProxy> proxies = LanguageSpecificTextProxy.fromList(m);
			return proxies;
		} else {
			return Collections.emptyList();
		}
	}
	
}
