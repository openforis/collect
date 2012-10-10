/**
 * 
 */
package org.openforis.collect.model;

import java.io.Serializable;

import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class CollectSurveyContext implements SurveyContext, Serializable {

	private static final long serialVersionUID = 1L;
	
	private ExpressionFactory expressionFactory;
	private Validator validator;
	private ExternalCodeListProvider externalCodeListProvider;

	public CollectSurveyContext(ExpressionFactory expressionFactory, Validator validator, ExternalCodeListProvider externalCodeListProvider) {
		this.expressionFactory = expressionFactory;
		this.validator = validator;
		this.externalCodeListProvider = externalCodeListProvider;
	}

	@Override
	public Survey createSurvey() {
		CollectSurvey survey = new CollectSurvey(this);
		return survey;
	}

	@Override
	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}

	public void setExpressionFactory(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
	}

	@Override
	public Validator getValidator() {
		return validator;
	}

	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	@Override
	public ExternalCodeListProvider getExternalCodeListProvider() {
		return externalCodeListProvider;
	}

	public void setExternalCodeListProvider(ExternalCodeListProvider externalCodeListProvider) {
		this.externalCodeListProvider = externalCodeListProvider;
	}

}
