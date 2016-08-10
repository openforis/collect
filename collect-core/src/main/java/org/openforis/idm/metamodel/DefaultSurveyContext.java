package org.openforis.idm.metamodel;

import java.util.Iterator;
import java.util.ServiceLoader;

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

	private static CoordinateOperations COORDINATE_OPERATIONS;

	static {
		ServiceLoader<CoordinateOperations> loader = ServiceLoader.load(CoordinateOperations.class);
		Iterator<CoordinateOperations> it = loader.iterator();
		if ( it.hasNext() ) {
			COORDINATE_OPERATIONS = it.next();
		}
	}

	private ExpressionFactory expressionFactory;
	private ExpressionEvaluator expressionEvaluator;
	private Validator validator;

	public DefaultSurveyContext() {
		expressionFactory = new ExpressionFactory();
		LookupProvider lookupProvider = new UnspecifiedLookupProvider();
		expressionFactory.setLookupProvider(lookupProvider);
		expressionEvaluator = new ExpressionEvaluator(expressionFactory);
		validator = new Validator();
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
	public CoordinateOperations getCoordinateOperations() {
		return COORDINATE_OPERATIONS;
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
