package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.ListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyValidationResultsVM {

	private List<SurveyValidationResult> validationResults;

	@Init
	public void init(@ExecutionArgParam("validationResults") List<SurveyValidationResult> validationResults) {
		this.validationResults = validationResults;
	}
	
	public List<SurveyValidationResult> getValidationResults() {
		return new ListModelList<SurveyValidationResult>(validationResults);
	}
	
}
