/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * The results of validating a single node
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class ValidationResults {

	private List<ValidationResult> errors;
	private List<ValidationResult> warnings;
	
	public ValidationResults() {
		errors = new ArrayList<ValidationResult>();
		warnings = new ArrayList<ValidationResult>();
	}

	public List<ValidationResult> getErrors() {
		return CollectionUtils.unmodifiableList(errors);
	}

	public List<ValidationResult> getWarnings() {
		return CollectionUtils.unmodifiableList(warnings);
	}

	public List<ValidationResult> getFailed() {
		List<ValidationResult> failed = new ArrayList<ValidationResult>(errors.size() + warnings.size());
		failed.addAll(errors);
		failed.addAll(warnings);
		return failed;
	}

	public void addResults(List<ValidationResult> results) {
		for (ValidationResult result : results) {
			addResult(result);
		}
	}
	
	public void addResults(ValidationResults other){
		errors.addAll(other.errors);
		warnings.addAll(other.warnings);
	}
	
	public void addResult(ValidationResult result) {
		switch (result.getFlag()) {
		case OK:
			// no-op
			break;
		case ERROR:
			errors.add(result);
			break;
		case WARNING:
			warnings.add(result);
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}

	public void addResult(ValidationRule<?> rule, ValidationResultFlag flag) {
		ValidationResult validationResult = new ValidationResult(rule, flag);
		addResult(validationResult);
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}
	
	@Override
	@SuppressWarnings("serial")
	public String toString() {
		return new HashMap<Object, Object>() {{
			put("errors", errors);
			put("warnings", warnings);
		}}.toString();
	}
}
