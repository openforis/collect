package org.openforis.idm.model;

import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.idm.metamodel.validation.CodeParentValidator;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * @author G. Miceli
 */
public class TestSurveyContext extends CollectSurveyContext {
	
	private static final long serialVersionUID = 1L;
	
	public TestLookupProvider lookupProvider;

	public TestSurveyContext() {
		super();
		lookupProvider = new TestLookupProvider();
		getExpressionFactory().setLookupProvider(lookupProvider);
		setValidator(new TestValidator());
		
	}
	
	private class TestValidator extends CollectValidator {
		@Override
		protected CodeParentValidator getCodeParentValidator() {
			return new TestCodeParentValidator();
		}
	}
	
	private class TestCodeParentValidator extends CodeParentValidator {
		@Override
		public ValidationResultFlag evaluate(CodeAttribute node) {
			return ValidationResultFlag.OK;
		}
	}
	
}
