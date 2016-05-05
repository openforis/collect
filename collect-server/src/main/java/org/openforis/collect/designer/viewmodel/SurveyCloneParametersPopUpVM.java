package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType.PUBLISHED;
import static org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType.TEMPORARY;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.form.validator.SurveyNameValidator;
import org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.SurveySummary;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
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
	private SurveySummary originalSurvey;
	
	//temporary variables
	private Map<String, Object> form;
	private Validator nameValidator;
	
	@Init
	public void init(@ExecutionArgParam("originalSurvey") SurveySummary originalSurvey) {
		this.originalSurvey = originalSurvey; 
		this.form = new HashMap<String, Object>();
		SurveyType originalSurveyType = originalSurvey.isTemporary() ? TEMPORARY: PUBLISHED;
		this.form.put("originalType", originalSurveyType.name());
		this.nameValidator = new SurveyNameValidator(surveyManager, SURVEY_NAME_FIELD, true);
	}

	@Command
	public void ok() throws IdmlParseException, SurveyValidationException {
		String newName = (String) form.get(SURVEY_NAME_FIELD);
		String originalType = (String) form.get(ORIGINAL_TYPE_FIELD);
		boolean originalSurveyIsWork = originalType.equals("TEMPORARY");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("newName", newName);
		args.put("originalSurveyIsWork", originalSurveyIsWork);
		BindUtils.postGlobalCommand(null, null, "performSelectedSurveyClone", args);
	}

	public SurveySummary getOriginalSurvey() {
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
