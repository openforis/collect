package org.openforis.collect.event;

import java.util.List;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;

public class ValidationResultsView {
	private List<ValidationResultView> errors;
	private List<ValidationResultView> warnings;
	
	public ValidationResultsView(List<ValidationResultView> errors, List<ValidationResultView> warnings) {
		this.errors = errors;
		this.warnings = warnings;
	}
	
	public List<ValidationResultView> getErrors() {
		return errors;
	}
	
	public List<ValidationResultView> getWarnings() {
		return warnings;
	}
	
	public static class ValidationResultView {
		private ValidationResultFlag flag;
		private String validator;
		
		public ValidationResultView(ValidationResultFlag flag, String validator) {
			super();
			this.flag = flag;
			this.validator = validator;
		}
		
		public ValidationResultFlag getFlag() {
			return flag;
		}
		
		public String getValidator() {
			return validator;
		}
	}
}
