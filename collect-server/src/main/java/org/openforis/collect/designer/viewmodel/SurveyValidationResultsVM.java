package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.designer.util.PopUpUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyValidationResultsVM {
	
	public static final String CONFIRM_EVENT_NAME = "onConfirm";
	private static final String DEFAULT_CONFIRM_BUTTON_LABEL_KEY = "global.confirm";

	private boolean showConfirm;
	private String confirmButtonLabel;
	private SurveyValidationResults validationResults;

	public static Window showPopUp(SurveyValidationResults validationResults, boolean showConfirm) {
		return showPopUp(validationResults, showConfirm, null);
	}
	
	public static Window showPopUp(SurveyValidationResults validationResults, boolean showConfirm, String confirmButtonLabel) {
		confirmButtonLabel = ObjectUtils.defaultIfNull(confirmButtonLabel, Labels.getLabel(DEFAULT_CONFIRM_BUTTON_LABEL_KEY));
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("showConfirm", showConfirm);
		args.put("validationResults", validationResults);
		args.put("confirmButtonLabel", confirmButtonLabel);
		return PopUpUtil.openPopUp(Resources.Component.SURVEY_VALIDATION_RESULTS_POPUP.getLocation(), true, args);
	}
	
	@Init
	public void init(@ExecutionArgParam("showConfirm") boolean showConfirm,
			@ExecutionArgParam("confirmButtonLabel") String confirmButtonLabel,
			@ExecutionArgParam("validationResults") SurveyValidationResults validationResults) {
		this.showConfirm = showConfirm;
		this.confirmButtonLabel = confirmButtonLabel;
		this.validationResults = validationResults;
	}
	
	@Command
	public void confirm(@ContextParam(ContextType.VIEW) Component view) {
		Events.postEvent(new ConfirmEvent(view));
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
	
	public String getConfirmButtonLabel() {
		return confirmButtonLabel;
	}

	public static class ConfirmEvent extends Event {
		
		private static final long serialVersionUID = 1L;

		public ConfirmEvent(Component target) {
			super(CONFIRM_EVENT_NAME, target);
		}
		
	}
}
