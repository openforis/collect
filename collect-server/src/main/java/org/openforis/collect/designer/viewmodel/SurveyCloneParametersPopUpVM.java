package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.form.validator.SurveyNameValidator;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyCloneParametersPopUpVM extends BaseVM {

	private static final String SURVEY_NAME_FIELD = "name";
	private static final String ORIGINAL_TYPE_FIELD = "originalType";

	@WireVariable 
	private SurveyManager surveyManager;
	
	//input
	private CollectSurvey originalSurvey;
	
	//temporary variables
	private Map<String, Object> form;
	private Validator nameValidator;
	
	@Init
	public void init(@ExecutionArgParam("originalSurvey") CollectSurvey originalSurvey) {
		this.originalSurvey = originalSurvey; 
		this.form = new HashMap<String, Object>();
		this.nameValidator = new SurveyNameValidator(surveyManager, SURVEY_NAME_FIELD, true);
	}

	@Command
	public void ok() throws IdmlParseException, SurveyValidationException {
		String newName = (String) form.get(SURVEY_NAME_FIELD);
		String originalType = (String) form.get(ORIGINAL_TYPE_FIELD);

		boolean originalSurveyIsWork = originalType.equals("TEMPORARY");
		
		CollectSurvey survey = surveyManager.duplicateSurveyForEdit(originalSurvey.getName(), originalSurveyIsWork, newName);

		//put survey in session and redirect into survey edit page
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(survey);
		sessionStatus.setCurrentLanguageCode(survey.getDefaultLanguage());
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}

	public CollectSurvey getOriginalSurvey() {
		return originalSurvey;
	}
	
	public Validator getNameValidator() {
		return nameValidator;
	}

	public Map<String, Object> getForm() {
		return form;
	}
	
	public void setForm(Map<String, Object> form) {
		this.form = form;
	}
	
}
