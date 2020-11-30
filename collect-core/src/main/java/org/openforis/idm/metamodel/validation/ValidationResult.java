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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((flag == null) ? 0 : flag.hashCode());
		result = prime * result + ((validator == null) ? 0 : validator.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValidationResult other = (ValidationResult) obj;
		if (flag != other.flag)
			return false;
		if (validator == null) {
			if (other.validator != null)
				return false;
		} else if (!validator.equals(other.validator))
			return false;
		return true;
	}
	
}
