package org.openforis.collect.model;

import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.SpeciesListService;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.ExpressionFactory;

public class CollectTestSurveyContext implements SurveyContext {

	private ExpressionFactory expressionFactory;
	public TestLookupProviderImpl lookupProvider;
	private ExpressionEvaluator expressionEvaluator;
	private CoordinateOperations coordinateOperations;	

	public CollectTestSurveyContext() {
		expressionFactory = new ExpressionFactory();
		lookupProvider = new TestLookupProviderImpl();
		expressionFactory.setLookupProvider(lookupProvider);
		expressionEvaluator = new ExpressionEvaluator(expressionFactory);
		
		coordinateOperations = new CoordinateOperations();
		coordinateOperations.initialize();
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
	public SpeciesListService getSpeciesListService() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public CoordinateOperations getCoordinateOperations() {
		return coordinateOperations;
	}
	
	@Override
	public Survey createSurvey() {
		return new CollectSurvey(this);
	}

}