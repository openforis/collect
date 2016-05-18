/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmParams;
import org.openforis.collect.designer.util.PageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.designer.viewmodel.SurveyValidationResultsVM.ConfirmEvent;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 * 
 */
public class SurveySelectVM extends BaseVM {

	
	public static final String CLOSE_SURVEY_IMPORT_POP_UP_GLOBAL_COMMNAD = "closeSurveyImportPopUp";

	public static final String UPDATE_SURVEY_LIST_COMMAND = "updateSurveyList";

	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private RecordManager recordManager;
	@WireVariable
	private CodeListManager codeListManager;
	@WireVariable
	private SurveyValidator surveyValidator;
	@WireVariable
	private CollectEarthSurveyValidator collectEarthSurveyValidator;

	private Window surveyImportPopUp;

	private Window jobStatusPopUp;
	
	private Window newSurveyParametersPopUp;

	private Window surveyClonePopup;

	private SurveySummary selectedSurvey;

	private List<SurveySummary> summaries;

	private SurveyCloneJob surveyCloneJob;

	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
		PageUtil.clearConfirmClose();
		loadSurveySummaries();
	}

	@Command
	public void editSelectedSurvey() throws IOException {
		CollectSurvey temporarySurvey = loadSelectedSurveyForEdit();
		SessionStatus sessionStatus = getSessionStatus();
		Integer publishedSurveyId = null;
		if (selectedSurvey.isPublished()) {
			if (selectedSurvey.isTemporary()) {
				publishedSurveyId = selectedSurvey.getPublishedId();
			} else {
				publishedSurveyId = selectedSurvey.getId();
			}
		}
		sessionStatus.setPublishedSurveyId(publishedSurveyId);
		sessionStatus.setSurvey(temporarySurvey);
		sessionStatus.setCurrentLanguageCode(null);
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}

	@Command
	public void newSurvey() throws IOException {
		if ( newSurveyParametersPopUp != null ) {
			closePopUp(newSurveyParametersPopUp);
			newSurveyParametersPopUp = null;
		}
		newSurveyParametersPopUp = openPopUp(
				Resources.Component.NEW_SURVEY_PARAMETERS_POPUP.getLocation(),
				true);
	}

	@Command
	public void exportSelectedSurvey() throws IOException {
		SurveyExportParametersVM.openPopUp(selectedSurvey);
	}

	protected void closeJobStatusPopUp() {
		closePopUp(jobStatusPopUp);
		jobStatusPopUp = null;
	}

	@Command
	public void cloneSelectedSurvey() throws IOException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("originalSurvey", selectedSurvey);
		surveyClonePopup = openPopUp(Resources.Component.SURVEY_CLONE_PARAMETERS_POPUP.getLocation(), true, args);
	}

	@GlobalCommand
	public void performSelectedSurveyClone(
			@BindingParam("newName") String newName, 
			@BindingParam("originalSurveyIsWork") Boolean originalSurveyIsWork) {
		surveyCloneJob = new SurveyCloneJob();
		surveyCloneJob.setOriginalSurvey(selectedSurvey);
		surveyCloneJob.setNewName(newName);
		surveyCloneJob.setOriginalSurveyIsWork(originalSurveyIsWork);
		jobManager.start(surveyCloneJob);
		
		closePopUp(surveyClonePopup);
		
		openSurveyCloneStatusPopUp(selectedSurvey.getName(), newName, surveyCloneJob);
	}
	
	protected void openSurveyCloneStatusPopUp(String originalSurveyName, String newSurveyName, Job job) {
		String title = Labels.getLabel("survey.clone.process_status_popup.message", new String[] { originalSurveyName, newSurveyName });
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(title, job, true);
	}
	
	@GlobalCommand
	public void jobAborted(@BindingParam("job") Job job) {
		if ( isJobStartedByThis(job)) {
			onJobEnd(job);
		}
	}
	
	@GlobalCommand
	public void jobFailed(@BindingParam("job") Job job) {
		if ( isJobStartedByThis(job)) {
			String errorMessage = job.getErrorMessage();
			MessageUtil.showError("global.job_status.failed.message", new String[]{errorMessage});
			onJobEnd(job);
		}
	}

	private void onJobEnd(Job job) {
		if (job == surveyCloneJob) {
			surveyCloneJob = null;
		}
		closeJobStatusPopUp();
	}

	private boolean isJobStartedByThis(Job job) {
		return job == surveyCloneJob;
	}
	
	@GlobalCommand
	public void jobCompleted(@BindingParam("job") Job job) {
		boolean jobStartedByThis = isJobStartedByThis(job);
		if (job == surveyCloneJob) {
			CollectSurvey survey = surveyCloneJob.getOutputSurvey();
			//put survey in session and redirect into survey edit page
			SessionStatus sessionStatus = getSessionStatus();
			sessionStatus.setSurvey(survey);
			sessionStatus.setCurrentLanguageCode(survey.getDefaultLanguage());
			Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
			surveyCloneJob = null;
		}
		if (jobStartedByThis) {
			onJobEnd(job);
		}
	}
	
	@Command
	public void publishSelectedSurvey() throws IOException {
		final CollectSurvey survey = loadSelectedSurvey();
		final CollectSurvey publishedSurvey = selectedSurvey.isPublished() ? surveyManager
				.getByUri(survey.getUri()) : null;
		SurveyValidator validator = getSurveyValidator(survey);
		SurveyValidationResults validationResults = validator.validateCompatibility(publishedSurvey, survey);
		if (validationResults.isOk()) {
			askConfirmThenPublishSurvey(survey);
		} else {
			final Window validationResultsPopUp = SurveyValidationResultsVM.showPopUp(validationResults, ! validationResults.hasErrors());
			validationResultsPopUp.addEventListener(SurveyValidationResultsVM.CONFIRM_EVENT_NAME, new EventListener<ConfirmEvent>() {
				public void onEvent(ConfirmEvent event) throws Exception {
					CollectSurvey survey = loadSelectedSurvey();
					askConfirmThenPublishSurvey(survey);
					closePopUp(validationResultsPopUp);
				}
			});
		}
	}

	private void askConfirmThenPublishSurvey(final CollectSurvey survey) {
		MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				performSurveyPublishing(survey);
			}
		}, "survey.publish.confirm");
		params.setOkLabelKey("survey.publish");
		MessageUtil.showConfirm(params);
	}

	@Command
	public void unpublishSelectedSurvey() throws IOException {
		final String surveyName = selectedSurvey.getName();
		//ask for a confirmation about survey unpublishing
		String messageKey = selectedSurvey.isTemporary() ? "survey.unpublish_overwrite_temporary.confirm" : "survey.unpublish.confirm";
		MessageUtil.ConfirmParams confirmParams = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
			public void onOk() {
				//ask for a second confirmation about records deletion
				ConfirmParams confirmParams2 = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
					public void onOk() {
						performSelectedSurveyUnpublishing();
					}
				});
				confirmParams2.setMessage("survey.delete_records.confirm", surveyName);
				confirmParams2.setOkLabelKey("survey.unpublish");
				confirmParams2.setTitle("survey.unpublish.confirm_title", surveyName);
				MessageUtil.showConfirm(confirmParams2);
			}
		}, messageKey);
		confirmParams.setMessage(messageKey, surveyName);
		confirmParams.setOkLabelKey("survey.unpublish");
		confirmParams.setTitle("survey.unpublish.confirm_title", surveyName);
		MessageUtil.showConfirm(confirmParams);
	}
	
	@Command
	public void deleteSelectedSurvey() {
		String messageKey;
		if (selectedSurvey.isTemporary()) {
			if (selectedSurvey.isPublished()) {
				messageKey = "survey.delete.published_work.confirm.message";
			} else {
				messageKey = "survey.delete.work.confirm.message";
			}
		} else {
			messageKey = "survey.delete.confirm.message";
		}
		MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
			public void onOk() {
				if (selectedSurvey.isPublished()) {
					//show a second confirmation about deleting all the records associated
					MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
						public void onOk() {
							performSelectedSurveyDeletion();
						}
					}, "survey.delete_records.confirm", new String[] { selectedSurvey.getName() }, 
					"survey.delete.confirm.title", (String[]) null, 
					"global.delete_item", "global.cancel");
				} else {
					performSelectedSurveyDeletion();
				}
			}
		}, messageKey, new String[] { selectedSurvey.getName() }, 
			"survey.delete.confirm.title", (String[]) null, 
			"global.delete_item", "global.cancel");
	}

	protected void performSelectedSurveyDeletion() {
		surveyManager.deleteSurvey(selectedSurvey.getId());
		selectedSurvey = null;
		loadSurveySummaries();
		notifyChange("selectedSurvey","surveySummaries");
	}

	protected void performSurveyPublishing(CollectSurvey survey) {
		try {
			surveyManager.publish(survey);
			loadSurveySummaries();
			selectedSurvey = null;
			notifyChange("selectedSurvey", "surveySummaries");
			Object[] args = new String[] { survey.getName() };
			MessageUtil.showInfo("survey.successfully_published", args);
			User user = getLoggedUser();
			surveyManager.validateRecords(survey.getId(), user);
		} catch (SurveyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	private void performSelectedSurveyUnpublishing() {
		try {
			Integer publishedSurveyId = selectedSurvey.isTemporary() ? selectedSurvey.getPublishedId() : selectedSurvey.getId();
			CollectSurvey temporarySurvey = surveyManager.unpublish(publishedSurveyId);
			loadSurveySummaries();
			selectedSurvey = null;
			notifyChange("selectedSurvey", "surveySummaries");
			Object[] args = new String[] { temporarySurvey.getName() };
			MessageUtil.showInfo("survey.successfully_unpublished", args);
		} catch (SurveyStoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Command
	public void goToIndex() {
		Executions.sendRedirect(Page.INDEX.getLocation());
	}

	@Command
	public void openSurveyImportPopUp() {
		surveyImportPopUp = openPopUp(
				Resources.Component.SURVEY_IMPORT_POPUP.getLocation(), true);
	}

	@GlobalCommand
	public void closeSurveyImportPopUp(
			@BindingParam("successfullyImported") Boolean successfullyImported) {
		if (surveyImportPopUp != null) {
			Binder binder = ComponentUtil.getBinder(surveyImportPopUp);
			SurveyImportVM vm = (SurveyImportVM) binder.getViewModel();
			vm.reset();
		}
		closePopUp(surveyImportPopUp);
		surveyImportPopUp = null;
		if (successfullyImported != null && successfullyImported.booleanValue()) {
			loadSurveySummaries();
			notifyChange("surveySummaries");
		}
	}

	@Command
	public void validateAllRecords() {
		User user = getLoggedUser();
		Integer publishedSurveyId = getSelectedPublishedSurveyId();
		surveyManager.validateRecords(publishedSurveyId, user);
		updateSurveyList();
	}

	private Integer getSelectedPublishedSurveyId() {
		return !selectedSurvey.isPublished() ? null
				: selectedSurvey.isTemporary() ? selectedSurvey.getPublishedId()
						: selectedSurvey.getId();
	}

	@Command
	public void cancelRecordValidation() {
		Integer selectedPublishedSurveyId = getSelectedPublishedSurveyId();
		surveyManager.cancelRecordValidation(selectedPublishedSurveyId);
		updateSurveyList();
	}

	@GlobalCommand
	public void updateSurveyList() {
		if ( surveyImportPopUp != null || jobStatusPopUp != null ) {
			//skip survey list update
			return;
		}
		try {
			List<SurveySummary> newSummaries = surveyManager.loadCombinedSummaries(null, true);
			if (summaries == null) {
				summaries = newSummaries;
			} else {
				for (SurveySummary newSummary : newSummaries) {
					SurveySummary oldSummary = findSummary(newSummary.getId(), newSummary.isPublished(), newSummary.isTemporary());
					if (oldSummary == null) {
						// TODO handle this??
					} else {
						oldSummary.setRecordValidationProcessStatus(newSummary
								.getRecordValidationProcessStatus());
						BindUtils.postNotifyChange(null, null, oldSummary,
								"recordValidationProgressStatus");
						BindUtils.postNotifyChange(null, null, oldSummary,
								"recordValidationInProgress");
						BindUtils.postNotifyChange(null, null, oldSummary,
								"recordValidationProgressPercent");
					}
				}
			}
		} catch (Exception e) {
			return;
		}

	}

	private void loadSurveySummaries() {
		summaries = surveyManager.loadCombinedSummaries(null, true);
	}

	private SurveySummary findSummary(Integer id, boolean published, boolean work) {
		for (SurveySummary summary : summaries) {
			if (summary.getId().equals(id)
					&& summary.isPublished() == published
					&& summary.isTemporary() == work) {
				return summary;
			}
		}
		return null;
	}

	protected CollectSurvey loadSelectedSurveyForEdit() {
		String uri = selectedSurvey.getUri();
		CollectSurvey temporarySurvey;
		if (selectedSurvey.isTemporary()) {
			temporarySurvey = surveyManager.loadSurvey(selectedSurvey.getId());
		} else if (selectedSurvey.isPublished()) {
			temporarySurvey = surveyManager.createTemporarySurveyFromPublished(uri);
		} else {
			throw new IllegalStateException(
					"Trying to load an invalid survey: " + uri);
		}
		return temporarySurvey;
	}

	protected CollectSurvey loadSelectedSurvey() {
		String uri = selectedSurvey.getUri();
		CollectSurvey survey;
		if (selectedSurvey.isTemporary()) {
			survey = surveyManager.loadSurvey(selectedSurvey.getId());
		} else {
			survey = surveyManager.getByUri(uri);
		}
		return survey;
	}

	public ListModel<SurveySummary> getSurveySummaries() {
		return new BindingListModelList<SurveySummary>(summaries, false);
	}

	public String getSurveyTooltip(SurveySummary summary) {
		return Labels.getLabel("surveys_list.tooltip", 
				new String[] {prettyDateFormat(summary.getCreationDate()), prettyDateFormat(summary.getModifiedDate())});
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
		return this.selectedSurvey == null || !this.selectedSurvey.isTemporary();
	}

	@DependsOn("selectedSurvey")
	public boolean isUnpublishDisabled() {
		return this.selectedSurvey == null || !this.selectedSurvey.isPublished();
	}

	private SurveyValidator getSurveyValidator(CollectSurvey survey) {
		return survey.getTarget() == SurveyTarget.COLLECT_EARTH ? collectEarthSurveyValidator : surveyValidator;
	}

	private class SurveyCloneJob extends Job {
		//input
		private SurveySummary originalSurvey;
		private boolean originalSurveyIsWork;
		private String newName;
		
		//ouptut
		private CollectSurvey outputSurvey;
		
		@Override
		protected void buildTasks() throws Throwable {
			addTask(new Task() {
				
				@Override
				protected void execute() throws Throwable {
					outputSurvey = surveyManager.duplicateSurveyIntoTemporary(originalSurvey.getName(), originalSurveyIsWork, newName);
				}
			});
		}
		
		public void setOriginalSurvey(SurveySummary originalSurvey) {
			this.originalSurvey = originalSurvey;
		}
		
		public void setOriginalSurveyIsWork(boolean originalSurveyIsWork) {
			this.originalSurveyIsWork = originalSurveyIsWork;
		}
		
		public void setNewName(String newName) {
			this.newName = newName;
		}
		
		public CollectSurvey getOutputSurvey() {
			return outputSurvey;
		}
	}
}
