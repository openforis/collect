package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType.PUBLISHED;
import static org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType.TEMPORARY;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.validator.SurveyNameValidator;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveySummary;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFileRandomGridGenerationParametersPopUpVM extends BaseVM {

	private static final String START_SURVEY_FILE_RANDOM_GRID_GENERATION_GLOBAL_COMMAND = "startSurveyFileRandomGridGeneration";
	private static final String CLOSE_SURVEY_FILE_RANDOM_GRID_GENERATION_GLOBAL_COMMAND = "closeRandomGridGenerationPopUp";

	public static final String PERCENTAGE_FIELD = "percentage";

	@WireVariable 
	private SurveyManager surveyManager;
	
	//input
	private SurveyFile sourceGridFile;
	
	//temporary variables
	private Map<String, Object> form;
	private Validator nameValidator;

	
	public static Window openPopUp(SurveyFile sourceGridFile) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("sourceGridFile", sourceGridFile);
		return openPopUp(Resources.Component.SURVEY_FILE_RANDOM_GRID_GENERATION_PARAMETERS_POPUP.getLocation(), true, args);
	}

	@Init(superclass = false)
	public void init(@ExecutionArgParam("sourceGridFile") SurveyFile sourceGridFile) {
		super.init();
		this.sourceGridFile = sourceGridFile; 
		this.form = new HashMap<String, Object>();
		this.form.put(PERCENTAGE_FIELD, 5);
	}
	
	@Command
	public void close() {
		BindUtils.postGlobalCommand(null, null, CLOSE_SURVEY_FILE_RANDOM_GRID_GENERATION_GLOBAL_COMMAND, null);
	}
	

	@Command
	public void start(@ContextParam(ContextType.BINDER) Binder binder) {
		Map<String, Object> args = new HashMap<String, Object>();
		Object percentage = getFormFieldValue(binder, PERCENTAGE_FIELD);
//		Double percentage = Double.valueOf(form.get(PERCENTAGE_FIELD).toString());
		args.put("percentage", percentage);
//		args.put("sourceGridFile", sourceGridFile);
		BindUtils.postGlobalCommand(null, null, START_SURVEY_FILE_RANDOM_GRID_GENERATION_GLOBAL_COMMAND, args);
	}

	public Map<String, Object> getForm() {
		return form;
	}
	
	public void setForm(Map<String, Object> form) {
		this.form = form;
	}
	
}
