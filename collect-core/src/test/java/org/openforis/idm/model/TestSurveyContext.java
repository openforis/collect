package org.openforis.idm.model;

import org.openforis.idm.metamodel.DefaultSurveyContext;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author G. Miceli
 */
public class TestSurveyContext extends DefaultSurveyContext {
	
	public TestLookupProvider lookupProvider;

	public TestSurveyContext() {
		super();
		ExpressionFactory expressionFactory = getExpressionFactory();
		lookupProvider = new TestLookupProvider();
		expressionFactory.setLookupProvider(lookupProvider);
	}
}
