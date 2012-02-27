/**
 * 
 */
package org.openforis.collect.model.validation;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.validation.Check.Flag;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.util.CollectionUtil;

/**
 * @author M. Togna
 * 
 */
public class CollectValidationResults extends ValidationResults {

	private List<ValidationResult> passed;
	private List<ValidationResult> errors;
	private List<ValidationResult> warnings;

	public CollectValidationResults() {
		passed = new ArrayList<ValidationResult>();
		errors = new ArrayList<ValidationResult>();
		warnings = new ArrayList<ValidationResult>();
	}

	public List<ValidationResult> getErrors() {
		return CollectionUtil.unmodifiableList(errors);
	}

	public List<ValidationResult> getWarnings() {
		return CollectionUtil.unmodifiableList(warnings);
	}

	public List<ValidationResult> getPassed() {
		return CollectionUtil.unmodifiableList(passed);
	}

	public List<ValidationResult> getFailed() {
		List<ValidationResult> failed = new ArrayList<ValidationResult>(errors.size() + warnings.size());
		failed.addAll(errors);
		failed.addAll(warnings);
		return failed;
	}

	void addFailed(ValidationResult result, Flag flag) {
		switch (flag) {
			case ERROR:
				errors.add(result);
				break;
			case WARN:
				warnings.add(result);
				break;
		}
	}

	void addErrors(List<ValidationResults> errors){
		errors.addAll(errors);
	}
	
	void addWarnings(List<ValidationResult> warnings){
		warnings.addAll(warnings);
	}
	
	void addPassed(List<ValidationResult> list) {
		passed.addAll(list);
	}
}
