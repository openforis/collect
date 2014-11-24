package org.openforis.collect.model;

import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.ExpressionFactory;

public class CollectTestSurveyContext implements SurveyContext {

	private ExpressionFactory expressionFactory;
	public TestLookupProviderImpl lookupProvider;
	private ExpressionEvaluator expressionEvaluator;

	public CollectTestSurveyContext() {
		expressionFactory = new ExpressionFactory();
		lookupProvider = new TestLookupProviderImpl();
		expressionFactory.setLookupProvider(lookupProvider);
		expressionEvaluator = new ExpressionEvaluator(expressionFactory);
	}

	@Override
	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}

	@Override
	public ExpressionEvaluator getExpressionEvaluator() {
		return expressionEvaluator;
	}
	
	@Override
	public Validator getValidator() {
		return new Validator();
	}

	@Override
	public ExternalCodeListProvider getExternalCodeListProvider() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CodeListService getCodeListService() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Survey createSurvey() {
		return new CollectSurvey(this);
	}

}