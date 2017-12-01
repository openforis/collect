/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.metamodel.SurveySummarySortField;
import org.openforis.collect.metamodel.SurveySummarySortField.Sortable;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listheader;
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
		reloadSurveySummaries(null);
	}

	@Command
	public void editSelectedSurvey() throws IOException {
		CollectSurvey temporarySurvey = loadSelectedSurveyForEdit();
		SurveyEditVM.redirectToSurveyEditPage(temporarySurvey.getId());
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
		surveyCloneJob.setActiveUser(getLoggedUser());
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
			MessageUtil.showError("global.job_status.failed.message", errorMessage);
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
			surveyCloneJob = null;
			SurveyEditVM.redirectToSurveyEditPage(survey.getId());
		}
		if (jobStartedByThis) {
			onJobEnd(job);
		}
	}
	
	@Command
	public void publishSelectedSurvey(@ContextParam(ContextType.BINDER) final Binder binder) throws IOException {
		final CollectSurvey survey = loadSelectedSurvey();
		final CollectSurvey publishedSurvey = selectedSurvey.isPublished() ? surveyManager
				.getByUri(survey.getUri()) : null;
		SurveyValidator validator = getSurveyValidator(survey);
		SurveyValidationResults validationResults = validator.validateCompatibility(publishedSurvey, survey);
		if (validationResults.isOk()) {
			askConfirmThenPublishSurvey(survey, binder);
		} else {
			final Window validationResultsPopUp = SurveyValidationResultsVM.showPopUp(validationResults, ! validationResults.hasErrors());
			validationResultsPopUp.addEventListener(SurveyValidationResultsVM.CONFIRM_EVENT_NAME, new EventListener<ConfirmEvent>() {
				public void onEvent(ConfirmEvent event) throws Exception {
					CollectSurvey survey = loadSelectedSurvey();
					askConfirmThenPublishSurvey(survey, binder);
					closePopUp(validationResultsPopUp);
				}
			});
		}
	}

	private void askConfirmThenPublishSurvey(final CollectSurvey survey, final Binder binder) {
		MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				performSurveyPublishing(survey, binder);
			}
		}, "survey.publish.confirm");
		params.setOkLabelKey("survey.publish");
		MessageUtil.showConfirm(params);
	}

	@Command
	public void unpublishSelectedSurvey(@ContextParam(ContextType.BINDER) final Binder binder) throws IOException {
		final String surveyName = selectedSurvey.getName();
		//ask for a confirmation about survey unpublishing
		String messageKey = selectedSurvey.isTemporary() ? "survey.unpublish_overwrite_temporary.confirm" : "survey.unpublish.confirm";
		MessageUtil.ConfirmParams confirmParams = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
			public void onOk() {
				//ask for a second confirmation about records deletion
				ConfirmParams confirmParams2 = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
					public void onOk() {
						performSelectedSurveyUnpublishing(binder);
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
	public void deleteSelectedSurvey(@ContextParam(ContextType.BINDER) final Binder binder) {
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
							performSelectedSurveyDeletion(binder);
						}
					}, "survey.delete_records.confirm", new String[] { selectedSurvey.getName() }, 
					"survey.delete.confirm.title", (String[]) null, 
					"global.delete_item", "global.cancel");
				} else {
					performSelectedSurveyDeletion(binder);
				}
			}
		}, messageKey, new String[] { selectedSurvey.getName() }, 
			"survey.delete.confirm.title", (String[]) null, 
			"global.delete_item", "global.cancel");
	}

	protected void performSelectedSurveyDeletion(Binder binder) {
		surveyManager.deleteSurvey(selectedSurvey.getId());
		selectedSurvey = null;
		notifyChange("selectedSurvey");
		reloadSurveySummaries(binder);
	}

	protected void performSurveyPublishing(CollectSurvey survey, Binder binder) {
		try {
			User loggedUser = getLoggedUser();
			surveyManager.publish(survey, loggedUser);
			selectedSurvey = null;
			notifyChange("selectedSurvey");
			reloadSurveySummaries(binder);
			MessageUtil.showInfo("survey.successfully_published", survey.getName());
			surveyManager.validateRecords(survey.getId(), loggedUser);
		} catch (SurveyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	private void performSelectedSurveyUnpublishing(Binder binder) {
		try {
			Integer publishedSurveyId = selectedSurvey.isTemporary() ? selectedSurvey.getPublishedId() : selectedSurvey.getId();
			CollectSurvey temporarySurvey = surveyManager.unpublish(publishedSurveyId, getLoggedUser());
			selectedSurvey = null;
			notifyChange("selectedSurvey");
			reloadSurveySummaries(binder);
			MessageUtil.showInfo("survey.successfully_unpublished", temporarySurvey.getName());
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
		Binder binder = null;
		if (surveyImportPopUp != null) {
			binder = ComponentUtil.getBinder(surveyImportPopUp);
			SurveyImportVM vm = (SurveyImportVM) binder.getViewModel();
			vm.reset();
		}
		closePopUp(surveyImportPopUp);
		surveyImportPopUp = null;
		if (successfullyImported != null && successfullyImported.booleanValue()) {
			reloadSurveySummaries(binder);
		}
	}

	@Command
	public void validateAllRecords() {
		Integer publishedSurveyId = getSelectedPublishedSurveyId();
		surveyManager.validateRecords(publishedSurveyId, getLoggedUser());
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
			List<SurveySummary> newSummaries = loadSurveySummaries(null);
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

	private void reloadSurveySummaries(Binder binder) {
		Listheader lastSortedHeader = null;
		boolean lastSortDirectionDescending = true;

		if (binder != null) {
			Component view = binder.getView();
			Iterable<Component> listHeaders = view.queryAll("listheader");
			for (Component headerComp : listHeaders) {
				Listheader listHeader = (Listheader) headerComp;
				String sortDirection = listHeader.getSortDirection();
				if (! "natural".equals(sortDirection)) {
					lastSortedHeader = listHeader;
					lastSortDirectionDescending = "descending".equals(sortDirection);
					break;
				}
			}
		}
		List<SurveySummarySortField> sortFields;
		if (lastSortedHeader == null) {
			sortFields = Arrays.asList(
					new SurveySummarySortField(Sortable.MODIFIED_DATE, true),
					new SurveySummarySortField(Sortable.NAME)
			);
		} else {
			String id = lastSortedHeader.getId();
			Sortable sortableField;
			if ("surveysListNameHeader".equals(id)) {
				sortableField = Sortable.NAME;
			} else if ("surveysListProjectNameHeader".equals(id)) {
				sortableField = Sortable.PROJECT_NAME;
			} else if ("surveysListLastModifiedDateHeader".equals(id)) {
				sortableField = Sortable.MODIFIED_DATE;
			} else if ("surveysListTargetHeader".equals(id)) {
				sortableField = Sortable.TARGET;
			} else if ("surveysListModifiedHeader".equals(id)) {
				sortableField = Sortable.MODIFIED;
			} else if ("surveysListPublishedHeader".equals(id)) {
				sortableField = Sortable.PUBLISHED;
			} else {
				throw new IllegalStateException("Unsupported sorting for column with header id " + id);
			}
			sortFields = Arrays.asList(
					new SurveySummarySortField(sortableField, lastSortDirectionDescending),
					new SurveySummarySortField(Sortable.NAME, lastSortDirectionDescending));
		}
		summaries = loadSurveySummaries(sortFields);
		notifyChange("surveySummaries");
		
		if (lastSortedHeader != null) {
			lastSortedHeader.sort(! lastSortDirectionDescending);
		}
	}

	private List<SurveySummary> loadSurveySummaries(List<SurveySummarySortField> sortFields) {
		return surveyManager.loadCombinedSummaries(null, true, getLoggedUser(), sortFields);
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
			temporarySurvey = surveyManager.createTemporarySurveyFromPublished(uri, getLoggedUser());
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
	
	public String getUserGroupLabel(UserGroup userGroup) {
		if (userGroup == null) {
			return null;
		} else if (UserGroupManager.DEFAULT_PUBLIC_USER_GROUP_NAME.equals(userGroup.getName())) { 
			return "Public";
		} else if (userGroupManager.getDefaultPrivateUserGroupName(getLoggedUser()).equals(userGroup.getName())) {
			return "Private";
		} else {
			return userGroup.getLabel();
		}
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
		private User activeUser;
		
		//ouptut
		private CollectSurvey outputSurvey;
		
		@Override
		protected void buildTasks() throws Throwable {
			addTask(new Task() {
				
				@Override
				protected void execute() throws Throwable {
					outputSurvey = surveyManager.duplicateSurveyIntoTemporary(originalSurvey.getName(), 
							originalSurveyIsWork, newName, activeUser);
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
		
		public void setActiveUser(User activeUser) {
			this.activeUser = activeUser;
		}
		
		public CollectSurvey getOutputSurvey() {
			return outputSurvey;
		}
	}
}
