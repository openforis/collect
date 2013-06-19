package org.openforis.collect.model;

import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.expression.ExpressionFactory;

public class TestSurveyContext implements SurveyContext {

	private ExpressionFactory expressionFactory;

	public TestSurveyContext() {
		expressionFactory = new ExpressionFactory();
		LookupProvider lookupProvider = new TestLookupProviderImpl();
		expressionFactory.setLookupProvider(lookupProvider);
	}

	@Override
	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
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