/**
 * 
 */
package org.openforis.collect.model;

import java.io.Serializable;
import java.util.UUID;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class CollectSurveyContext implements SurveyContext, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_URI_PREFIX = "http://www.openforis.org/idm/";
	
	private transient ExpressionFactory expressionFactory;
	private transient Validator validator;
	private transient ExternalCodeListProvider externalCodeListProvider;
	private transient CodeListService codeListService;

	private String uriPrefix;

	public CollectSurveyContext(ExpressionFactory expressionFactory, Validator validator) {
		this.expressionFactory = expressionFactory;
		this.validator = validator;
		this.uriPrefix = DEFAULT_URI_PREFIX;
	}
	
	@Override
	public Survey createSurvey() {
		CollectSurvey survey = new CollectSurvey(this);
		//set URI
		UUID uuid = UUID.randomUUID();
		survey.setUri(uriPrefix + uuid.toString());
		//application options
		UIOptions uiOptions = survey.createUIOptions();
		survey.addApplicationOptions(uiOptions);
		return survey;
	}

	@Override
	public ExpressionFactory getExpressionFactory() {
		return expressionFactory;
	}

	public void setExpressionFactory(ExpressionFactory expressionFactory) {
		this.expressionFactory = expressionFactory;
	}

	@Override
	public Validator getValidator() {
		return validator;
	}

	public void setValidator(Validator validator) {
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
	
	public void setCodeListService(
			CodeListService codeListService) {
		this.codeListService = codeListService;
	}

	public String getUriPrefix() {
		return uriPrefix;
	}
	
	public void setUriPrefix(String uriPrefix) {
		this.uriPrefix = uriPrefix;
	}
	
}
