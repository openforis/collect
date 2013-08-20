/**
 * 
 */
package org.openforis.collect.model;

import java.io.Serializable;

import org.openforis.idm.metamodel.CodeListService;
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
	
	private transient ExpressionFactory expressionFactory;
	private transient Validator validator;
	private transient ExternalCodeListProvider externalCodeListProvider;
	private transient CodeListService codeListService;

	public CollectSurveyContext(ExpressionFactory expressionFactory, Validator validator) {
		this.expressionFactory = expressionFactory;
		this.validator = validator;
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

	@Override
	public CodeListService getCodeListService() {
		return codeListService;
	}
	
	public void setCodeListService(
			CodeListService codeListService) {
		this.codeListService = codeListService;
	}
	
}
