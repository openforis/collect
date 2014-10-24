package org.openforis.idm.metamodel;

import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author G. Miceli
 */
public class DefaultSurveyContext implements SurveyContext {

	private ExpressionFactory expressionFactory;

	public DefaultSurveyContext() {
		expressionFactory = new ExpressionFactory();
		LookupProvider lookupProvider = new UnspecifiedLookupProvider();
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
