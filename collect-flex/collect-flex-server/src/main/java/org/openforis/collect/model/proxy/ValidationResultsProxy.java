/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.metamodel.validation.ValidationResults;

/**
 * @author M. Togna
 * 
 */
public class ValidationResultsProxy implements Proxy {

	private transient ValidationResults validationResults;

	public ValidationResultsProxy(ValidationResults validationResults) {
		this.validationResults = validationResults;
	}

	@ExternalizedProperty
	public List<ValidationResultProxy> getErrors() {
		return ValidationResultProxy.fromList(validationResults.getErrors());
	}

	@ExternalizedProperty
	public List<ValidationResultProxy> getWarnings() {
		return ValidationResultProxy.fromList(validationResults.getWarnings());
	}

	@ExternalizedProperty
	public List<ValidationResultProxy> getPassed() {
		return ValidationResultProxy.fromList(validationResults.getPassed());
	}

	@ExternalizedProperty
	public List<ValidationResultProxy> getFailed() {
		return ValidationResultProxy.fromList(validationResults.getFailed());
	}

}
