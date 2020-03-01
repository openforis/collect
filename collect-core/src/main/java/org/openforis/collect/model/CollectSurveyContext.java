/**
 * 
 */
package org.openforis.collect.model;

import java.io.Serializable;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.SpeciesListService;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class CollectSurveyContext implements SurveyContext<CollectSurvey>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private transient ExpressionFactory expressionFactory;
	private transient ExpressionEvaluator expressionEvaluator;
	private transient Validator validator;
	private transient ExternalCodeListProvider externalCodeListProvider;
	private transient CodeListService codeListService;
	private transient SpeciesListService speciesListService;
	private transient CoordinateOperations coordinateOperations;

	public CollectSurveyContext() {
		this(new ExpressionFactory(), new CollectValidator());
	}

	public CollectSurveyContext(ExpressionFactory expressionFactory, Validator validator) {
		this(expressionFactory, validator, (CodeListService) null, (SpeciesListService) null);
	}
	
	public CollectSurveyContext(ExpressionFactory expressionFactory, Validator validator, CodeListService codeListService) {
		this(expressionFactory, validator, codeListService, (SpeciesListService) null);
	}
	
	public CollectSurveyContext(ExpressionFactory expressionFactory, Validator validator, 
			CodeListService codeListService, SpeciesListService speciesListService) {
		this.expressionFactory = expressionFactory;
		this.validator = validator;
		this.codeListService = codeListService;
		this.expressionEvaluator = new ExpressionEvaluator(expressionFactory);
		this.coordinateOperations = new CoordinateOperations();
		this.coordinateOperations.initialize();
	}
	
	@Override
	public CollectSurvey createSurvey() {
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
	public SpeciesListService getSpeciesListService() {
		return speciesListService;
	}
	
	public void setSpeciesListService(SpeciesListService speciesListService) {
		this.speciesListService = speciesListService;
	}
	
	@Override
	public CoordinateOperations getCoordinateOperations() {
		return coordinateOperations;
	}
	
	public void setCoordinateOperations(CoordinateOperations coordinateOperations) {
		this.coordinateOperations = coordinateOperations;
	}
	
}
