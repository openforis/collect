package org.openforis.idm.metamodel;

import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author G. Miceli
 * @author S. Ricci
 */
public class DefaultSurveyContext implements SurveyContext {

	private ExpressionFactory expressionFactory;
	private ExpressionEvaluator expressionEvaluator;
	private Validator validator;
	private static CoordinateOperations coordinateOperations;

	public DefaultSurveyContext() {
		coordinateOperations = new CoordinateOperations();
		expressionFactory = new ExpressionFactory();
		LookupProvider lookupProvider = new UnspecifiedLookupProvider();
		expressionFactory.setLookupProvider(lookupProvider);
		expressionEvaluator = new ExpressionEvaluator(expressionFactory);
		validator = new Validator();
		
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
		return validator;
	}

	@Override
	public ExternalCodeListProvider getExternalCodeListProvider() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public CodeListService getCodeListService() {
		return null;
	}
	
	@Override
	public SpeciesListService getSpeciesListService() {
		return null;
	}
	
	@Override
	public CoordinateOperations getCoordinateOperations() {
		return coordinateOperations;
	}

	@Override
	public Survey createSurvey() {
		return new Survey(this);
	}
	
	public class UnspecifiedLookupProvider implements LookupProvider {
		
		@Override
		public Object lookup(Survey survey, String name, String attribute, Object... keys) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Coordinate lookupSamplingPointCoordinate(Survey survey, String... keys) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object lookupSamplingPointData(Survey survey, String attribute, String... keys) {
			throw new UnsupportedOperationException();
		}
		
	}
}
