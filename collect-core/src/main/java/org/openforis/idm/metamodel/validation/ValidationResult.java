package org.openforis.idm.metamodel.validation;


/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class ValidationResult {

	private ValidationRule<?> validator;
	private ValidationResultFlag flag;
	
	public ValidationResult(ValidationRule<?> validator, ValidationResultFlag flag) {
		this.validator = validator;
		this.flag = flag;
	}

	public ValidationRule<?> getValidator() {
		return validator;
	}

	public ValidationResultFlag getFlag() {
		return flag;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(validator);
		sb.append(flag);
		return sb.toString();
	}
}
