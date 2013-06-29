/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.PageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyImportException;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveySelectVM extends BaseVM {
	
	private static final String TEXT_XML = "text/xml";

	public static final String CLOSE_SURVEY_IMPORT_POP_UP_GLOBAL_COMMNAD = "closeSurveyImportPopUp";
	
	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private SurveyValidator surveyValidator;
	
	private SurveySummary selectedSurvey;

	private Window surveyImportPopUp;

	private Window validationResultsPopUp;
	
	@Init()
	public void init() {
		PageUtil.clearConfirmClose();
	}
	
	@Command
	public void editSelectedSurvey() throws IOException {
		CollectSurvey surveyWork = loadSelectedSurveyForEdit();
		SessionStatus sessionStatus = getSessionStatus();
		Integer publishedSurveyId = null;
		if ( selectedSurvey.isPublished() ) {
			if ( selectedSurvey.isWork() ) {
				publishedSurveyId = selectedSurvey.getPublishedId();
			} else {
				publishedSurveyId = selectedSurvey.getId();
			}
		}
		sessionStatus.setPublishedSurveyId(publishedSurveyId);
		sessionStatus.setSurvey(surveyWork);
		sessionStatus.setCurrentLanguageCode(null);
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}

	@Command
	public void newSurvey() throws IOException {
		CollectSurvey survey = surveyManager.createSurveyWork();
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(survey);
		sessionStatus.setCurrentLanguageCode(null);
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}
	
	@Command
	public void exportSelectedSurvey() throws IOException {
		CollectSurvey survey = loadSelectedSurvey();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		surveyManager.marshalSurvey(survey, os, true, true, false);
		byte[] content = os.toByteArray();
		String fileName = survey.getName() + ".xml";
		Filedownload.save(content, TEXT_XML, fileName);
	}
	
	@Command
	public void publishSelectedSurvey() throws IOException {
		final CollectSurvey survey = loadSelectedSurvey();
		final CollectSurvey publishedSurvey = selectedSurvey.isPublished() ? surveyManager.getByUri(survey.getUri()): null;
		if ( validateSurvey(survey, publishedSurvey) ) {
			MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performSurveyPublishing(survey);
				}
			}, "survey.publish.confirm");
		}
	}
	
	@Command
	public void deleteSelectedSurvey() {
		String messageKey;
		if ( selectedSurvey.isWork() ) {
			if ( selectedSurvey.isPublished() ) {
				messageKey = "survey.delete.published_work.confirm";
			} else {
				messageKey = "survey.delete.work.confirm";
			}
		} else {
			messageKey = "survey.delete.confirm";
		}
		MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				performSelectedSurveyDeletion();
			}
		}, messageKey, new String[]{selectedSurvey.getName()});
	}
	
	protected void performSelectedSurveyDeletion() {
		if ( selectedSurvey.isWork() ) {
			surveyManager.deleteSurveyWork(selectedSurvey.getId());
		} else {
			surveyManager.deleteSurvey(selectedSurvey.getId());
		}
		notifyChange("surveySummaries");
	}

	protected boolean validateSurvey(CollectSurvey survey, CollectSurvey oldPublishedSurvey) {
		List<SurveyValidationResult> validationResults = surveyValidator.validateCompatibility(oldPublishedSurvey, survey);
		if ( validationResults.isEmpty() ) {
			return true;
		} else {
			openValidationResultsPopUp(validationResults);
			return false;
		}
	}
	
	@GlobalCommand
	public void openValidationResultsPopUp(@BindingParam("validationResults") List<SurveyValidationResult> validationResults) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("validationResults", validationResults);
		validationResultsPopUp = openPopUp(Resources.Component.SURVEY_VALIDATION_RESULTS_POPUP.getLocation(), true, args);
	}
	
	@GlobalCommand
	public void closeValidationResultsPopUp() {
		closePopUp(validationResultsPopUp);
		validationResultsPopUp = null;
	}
	
	protected void performSurveyPublishing(CollectSurvey survey) {
		try {
			surveyManager.publish(survey);
			notifyChange("surveySummaries");
			Object[] args = new String[]{survey.getName()};
			MessageUtil.showInfo("survey.successfully_published", args);
		} catch (SurveyImportException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Command
	public void goToIndex() {
		Executions.sendRedirect(Page.INDEX.getLocation());
	}
	
	@Command
	public void openSurveyImportPopUp() {
		surveyImportPopUp = openPopUp(Resources.Component.SURVEY_IMPORT_POPUP.getLocation(), true);
	}
	
	@GlobalCommand
	public void closeSurveyImportPopUp(@BindingParam("successfullyImported") Boolean successfullyImported) {
		if ( surveyImportPopUp != null ) {
			Binder binder = ComponentUtil.getBinder(surveyImportPopUp);
			SurveyImportVM vm = (SurveyImportVM) binder.getViewModel();
			vm.reset();
		}
		closePopUp(surveyImportPopUp);
		surveyImportPopUp = null;
		if ( successfullyImported != null && successfullyImported.booleanValue()) {
			notifyChange("surveySummaries");
		}
	}
	
	protected CollectSurvey loadSelectedSurveyForEdit() {
		String uri = selectedSurvey.getUri();
		CollectSurvey surveyWork;
		if ( selectedSurvey.isWork() ) {
			surveyWork = surveyManager.loadSurveyWork(selectedSurvey.getId());
		} else if ( selectedSurvey.isPublished() ) {
			surveyWork = surveyManager.duplicatePublishedSurveyForEdit(uri);
		} else {
			throw new IllegalStateException("Trying to load an invalid survey: " + uri);
		}
		return surveyWork;
	}
	
	protected CollectSurvey loadSelectedSurvey() {
		String uri = selectedSurvey.getUri();
		CollectSurvey survey;
		if ( selectedSurvey.isWork() ) {
			survey = surveyManager.loadSurveyWork(selectedSurvey.getId());
		} else {
			survey = surveyManager.getByUri(uri);
		}
		return survey;
	}
	
	public ListModel<SurveySummary> getSurveySummaries() {
		List<SurveySummary> summaries = surveyManager.loadSummaries();
		return new BindingListModelList<SurveySummary>(summaries, false);
	}

	public SurveySummary getSelectedSurvey() {
		return selectedSurvey;
	}

	public void setSelectedSurvey(SurveySummary selectedSurvey) {
		this.selectedSurvey = selectedSurvey;
	}
	
	@DependsOn("selectedSurvey")
	public boolean isSurveySelected() {
		return this.selectedSurvey != null;
	}
	
	@DependsOn("selectedSurvey")
	public boolean isEditingDisabled() {
		return this.selectedSurvey == null;
	}
	
	@DependsOn("selectedSurvey")
	public boolean isExportDisabled() {
		return this.selectedSurvey == null;
	}

	@DependsOn("selectedSurvey")
	public boolean isPublishDisabled() {
		return this.selectedSurvey == null || ! this.selectedSurvey.isWork();
	}

}
