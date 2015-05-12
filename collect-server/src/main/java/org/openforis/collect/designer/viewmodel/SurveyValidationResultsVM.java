package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyValidationResultsVM {

	private boolean showConfirm;
	private SurveyValidationResults validationResults;

	public static Window showPopUp(SurveyValidationResults validationResults, boolean showConfirm) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("showConfirm", showConfirm);
		args.put("validationResults", validationResults);
		return PopUpUtil.openPopUp(Resources.Component.SURVEY_VALIDATION_RESULTS_POPUP.getLocation(), true, args);
	}
	
	@Init
	public void init(@ExecutionArgParam("showConfirm") boolean showConfirm,
			@ExecutionArgParam("validationResults") SurveyValidationResults validationResults) {
		this.showConfirm = showConfirm;
		this.validationResults = validationResults;
	}
	
	public List<SurveyValidationResult> getResults() {
		return new ListModelList<SurveyValidationResult>(validationResults.getResults());
	}
	
	public boolean hasOnlyWarnings() {
		return ! validationResults.hasErrors();
	}
	
	public boolean isShowConfirm() {
		return this.showConfirm;
	}
	
}
