/**
 * 
 */
package org.openforis.collect.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class CollectSurveyContext implements SurveyContext, Serializable {

	private static CoordinateOperations COORDINATE_OPERATIONS;

	static {
		ServiceLoader<CoordinateOperations> loader = ServiceLoader.load(CoordinateOperations.class);
		Iterator<CoordinateOperations> it = loader.iterator();
		if ( it.hasNext() ) {
			COORDINATE_OPERATIONS = it.next();
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	private transient ExpressionFactory expressionFactory;
	private transient ExpressionEvaluator expressionEvaluator;
	private transient Validator validator;
	private transient ExternalCodeListProvider externalCodeListProvider;
	private transient CodeListService codeListService;

	public CollectSurveyContext() {
		this(new ExpressionFactory(), new CollectValidator());
	}

	public CollectSurveyContext(ExpressionFactory expressionFactory, Validator validator) {
		this(expressionFactory, validator, (CodeListService) null);
	}
	
	public CollectSurveyContext(ExpressionFactory expressionFactory, Validator validator, CodeListService codeListService) {
		this.expressionFactory = expressionFactory;
		this.validator = validator;
		this.codeListService = codeListService;
		this.expressionEvaluator = new ExpressionEvaluator(expressionFactory);
	}
	
	@Override
	public Survey createSurvey() {
		CollectSurvey survey = new CollectSurvey(this);
		//application options
		UIOptions uiOptions = survey.createUIOptions();
		survey.addApplicationOptions(uiOptions);
		return survey;
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

	public void setValidator(CollectValidator validator) {
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
	
	public void setCodeListService(CodeListService codeListService) {
		this.codeListService = codeListService;
	}

	@Override
	public CoordinateOperations getCoordinateOperations() {
		return COORDINATE_OPERATIONS;
	}
	
}
