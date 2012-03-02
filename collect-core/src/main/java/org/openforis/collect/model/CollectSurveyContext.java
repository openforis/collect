/**
 * 
 */
package org.openforis.collect.model;

import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author M. Togna
 * 
 */
public class CollectSurveyContext implements SurveyContext {

	private ExpressionFactory expressionFactory;
	private Validator validator;
	
	public CollectSurveyContext(ExpressionFactory expressionFactory, Validator validator) {
		this.expressionFactory = expressionFactory;
		this.validator = validator;
	}

	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}

	public void setExpressionFactory(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
	}

	public Validator getValidator() {
		return validator;
	}

	public void setValidator(Validator validator) {
		this.validator = validator;
	}

}
