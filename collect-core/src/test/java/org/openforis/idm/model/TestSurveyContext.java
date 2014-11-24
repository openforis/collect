package org.openforis.idm.model;

import org.openforis.idm.metamodel.DefaultSurveyContext;

/**
 * @author G. Miceli
 */
public class TestSurveyContext extends DefaultSurveyContext {
	
	public TestLookupProvider lookupProvider;

	public TestSurveyContext() {
		super();
		lookupProvider = new TestLookupProvider();
		getExpressionFactory().setLookupProvider(lookupProvider);
	}
}
