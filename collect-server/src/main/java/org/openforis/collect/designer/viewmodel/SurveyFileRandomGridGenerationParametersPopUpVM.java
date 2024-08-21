package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.SurveyFile;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
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
	public static final String NEXT_MEASUREMENT_FIELD = "nextMeasurement";

	@WireVariable 
	private SurveyManager surveyManager;
	
	//input
	private SurveyFile sourceGridFile;
	
	//temporary variables
	private Map<String, Object> form;

	
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
		Object nextMeasurement = getFormFieldValue(binder, NEXT_MEASUREMENT_FIELD);
		args.put("percentage", percentage);
		args.put("nextMeasurement", nextMeasurement);
		BindUtils.postGlobalCommand(null, null, START_SURVEY_FILE_RANDOM_GRID_GENERATION_GLOBAL_COMMAND, args);
	}

	public Map<String, Object> getForm() {
		return form;
	}
	
	public void setForm(Map<String, Object> form) {
		this.form = form;
	}
	
}
