/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.Proxy;
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

	public String getRuleName() {
		return validationResult.getValidator().getClass().getSimpleName();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getLocalizedMessage() {
		ValidationRule<?> v = validationResult.getValidator();
		if ( v instanceof Check ) {
			List<String> m = ((Check) v).getMessages();
			if ( m.isEmpty() ) {
				return null;
			} else {
				return m.get(0);
			}
		} else {
			return null;
		}
	}
	
	@Deprecated
	public boolean isValid() {
		return false;
	}

}
