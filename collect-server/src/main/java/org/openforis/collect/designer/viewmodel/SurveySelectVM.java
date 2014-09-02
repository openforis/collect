/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.PageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyBackupJob.OutputFormat;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.utils.Dates;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.spring.SpringJobManager;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.logging.Log;
import org.zkoss.util.resource.Labels;
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

	/**
	 * Pattern for survey export file name (SURVEYNAME_DATE.OUTPUTFORMAT)
	 */
	private static final String SURVEY_EXPORT_FILE_NAME_PATTERN = "%s_%s.%s";

	private static final String TEMPORARY_SURVEY_TYPE = "temporary";

	private static Log log = Log.lookup(SurveySelectVM.class);

	public static final String CLOSE_SURVEY_IMPORT_POP_UP_GLOBAL_COMMNAD = "closeSurveyImportPopUp";

	public static final String UPDATE_SURVEY_LIST_COMMAND = "updateSurveyList";

	@WireVariable
	private SurveyManager surveyManager;

	@WireVariable
	private SurveyValidator surveyValidator;

	@WireVariable
	private SpringJobManager springJobManager;

	private SurveySummary selectedSurvey;

	private Window surveyImportPopUp;

	private Window validationResultsPopUp;

	private List<SurveySummary> summaries;

	private SurveyBackupJob surveyBackupJob;

	private Window jobStatusPopUp;
	private Window newSurveyParametersPopUp;

	private Window surveyExportPopup;

	@Init()
	public void init() {
		PageUtil.clearConfirmClose();
		loadSurveySummaries();
	}

	@Command
	public void editSelectedSurvey() throws IOException {
		CollectSurvey surveyWork = loadSelectedSurveyForEdit();
		SessionStatus sessionStatus = getSessionStatus();
		Integer publishedSurveyId = null;
		if (selectedSurvey.isPublished()) {
			if (selectedSurvey.isWork()) {
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
		//set default parameters
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("survey", selectedSurvey);
		surveyExportPopup = openPopUp(Resources.Component.SURVEY_EXPORT_PARAMETERS_POPUP.getLocation(), true, args);
	}
	
	@GlobalCommand
	public void performSelectedSurveyExport(@BindingParam("parameters") SurveyExportParametersFormObject parameters) {
		String uri = selectedSurvey.getUri();
		CollectSurvey survey;
		if ( selectedSurvey.isWork() && parameters.getType().equals(TEMPORARY_SURVEY_TYPE) ) {
			survey = surveyManager.loadSurveyWork(selectedSurvey.getId());
		} else {
			survey = surveyManager.getByUri(uri);
		}
		Integer surveyId = survey.getId();
		surveyBackupJob = springJobManager.createJob(SurveyBackupJob.class);
		surveyBackupJob.setSurvey(survey);
		surveyBackupJob.setIncludeData(parameters.isIncludeData());
		surveyBackupJob.setIncludeRecordFiles(parameters.isIncludeUploadedFiles());
		surveyBackupJob.setOutputFormat(OutputFormat.valueOf(parameters.getOutputFormat()));
		
		springJobManager.start(surveyBackupJob, String.valueOf(surveyId));
		
		closePopUp(surveyExportPopup);
		surveyExportPopup = null;
		
		openSurveyExportStatusPopUp();
	}

	protected void openSurveyExportStatusPopUp() {
		String surveyName = surveyBackupJob.getSurvey().getName();
		String title = Labels.getLabel("survey.export_survey.process_status_popup.message", new String[] { surveyName });
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(title, surveyBackupJob, true);
	}

	protected void closeJobStatusPopUp() {
		closePopUp(jobStatusPopUp);
		jobStatusPopUp = null;
	}

	@GlobalCommand
	public void jobAborted(@BindingParam("job") Job job) {
		closeJobStatusPopUp();
		surveyBackupJob = null;
	}
	
	@GlobalCommand
	public void jobFailed(@BindingParam("job") Job job) {
		closeJobStatusPopUp();
		if ( job == surveyBackupJob ) {
			surveyBackupJob = null;
			String errorMessage = job.getErrorMessage();
			MessageUtil.showError("global.job_status.failed.message", new String[]{errorMessage});
		}
	}
	
	@GlobalCommand
	public void jobCompleted(@BindingParam("job") Job job) {
		closeJobStatusPopUp();
		if ( job == surveyBackupJob ) {
			surveyExportJobCompleted();
		}
	}
	
	private void surveyExportJobCompleted() {
		File file = surveyBackupJob.getOutputFile();
		CollectSurvey survey = surveyBackupJob.getSurvey();
		String surveyName = survey.getName();
		String dateStr = Dates.formatDateTimeToXML(new Date());
		String extension = surveyBackupJob.getOutputFormat().getOutputFileExtension();
		String fileName = String.format(SURVEY_EXPORT_FILE_NAME_PATTERN, surveyName, dateStr, extension);
		String contentType = "application/octet-stream";
		try {
			FileInputStream is = new FileInputStream(file);
			Filedownload.save(is, contentType, fileName);
		} catch (FileNotFoundException e) {
			log.error(e);
			MessageUtil.showError("survey.export_survey.error", new String[]{e.getMessage()});
		} finally {
			surveyBackupJob = null;
		}
	}
	
	@Command
	public void publishSelectedSurvey() throws IOException {
		final CollectSurvey survey = loadSelectedSurvey();
		final CollectSurvey publishedSurvey = selectedSurvey.isPublished() ? surveyManager
				.getByUri(survey.getUri()) : null;
		if (validateSurvey(survey, publishedSurvey)) {
			MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performSurveyPublishing(survey);
				}
			}, "survey.publish.confirm");
			params.setOkLabelKey("survey.publish");
			MessageUtil.showConfirm(params);
		}
	}

	@Command
	public void deleteSelectedSurvey() {
		String messageKey;
		if (selectedSurvey.isWork()) {
			if (selectedSurvey.isPublished()) {
				messageKey = "survey.delete.published_work.confirm.message";
			} else {
				messageKey = "survey.delete.work.confirm.message";
			}
		} else {
			messageKey = "survey.delete.confirm.message";
		}
		MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				performSelectedSurveyDeletion();
			}
		}, messageKey, new String[] { selectedSurvey.getName() }, 
			"survey.delete.confirm.title", (String[]) null, 
			"global.delete_item", "global.cancel");
	}

	protected void performSelectedSurveyDeletion() {
		if (selectedSurvey.isWork()) {
			surveyManager.deleteSurveyWork(selectedSurvey.getId());
		} else {
			surveyManager.deleteSurvey(selectedSurvey.getId());
		}
		selectedSurvey = null;
		loadSurveySummaries();
		notifyChange("selectedSurvey","surveySummaries");
	}

	protected boolean validateSurvey(CollectSurvey survey,
			CollectSurvey oldPublishedSurvey) {
		SurveyValidationResults validationResults = surveyValidator.validateCompatibility(oldPublishedSurvey, survey);
		if (validationResults.isOk()) {
			return true;
		} else {
			validationResultsPopUp = SurveyValidationResultsVM.showPopUp(validationResults, ! validationResults.hasErrors());
			return false;
		}
	}

	@GlobalCommand
	public void confirmValidationResultsPopUp() {
		closePopUp(validationResultsPopUp);
		validationResultsPopUp = null;
		CollectSurvey survey = loadSelectedSurvey();
		performSurveyPublishing(survey);
	}
	
	@GlobalCommand
	public void closeValidationResultsPopUp() {
		closePopUp(validationResultsPopUp);
		validationResultsPopUp = null;
	}

	protected void performSurveyPublishing(CollectSurvey survey) {
		try {
			surveyManager.publish(survey);
			loadSurveySummaries();
			notifyChange("surveySummaries");
			Object[] args = new String[] { survey.getName() };
			MessageUtil.showInfo("survey.successfully_published", args);
			User user = getLoggedUser();
			surveyManager.validateRecords(survey.getId(), user);
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
				: selectedSurvey.isWork() ? selectedSurvey.getPublishedId()
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
		List<SurveySummary> newSummaries = surveyManager.loadSummaries(null, true);
		if (summaries == null) {
			summaries = newSummaries;
		} else {
			for (SurveySummary newSummary : newSummaries) {
				SurveySummary oldSummary = findSummary(summaries,
						newSummary.getId(), newSummary.isPublished(),
						newSummary.isWork());
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
	}

	private void loadSurveySummaries() {
		summaries = surveyManager.loadSummaries(null, true);
	}

	private SurveySummary findSummary(List<SurveySummary> summaries2,
			Integer id, boolean published, boolean work) {
		for (SurveySummary summary : summaries) {
			if (summary.getId().equals(id)
					&& summary.isPublished() == published
					&& summary.isWork() == work) {
				return summary;
			}
		}
		return null;
	}

	protected CollectSurvey loadSelectedSurveyForEdit() {
		String uri = selectedSurvey.getUri();
		CollectSurvey surveyWork;
		if (selectedSurvey.isWork()) {
			surveyWork = surveyManager.loadSurveyWork(selectedSurvey.getId());
		} else if (selectedSurvey.isPublished()) {
			surveyWork = surveyManager.duplicatePublishedSurveyForEdit(uri);
		} else {
			throw new IllegalStateException(
					"Trying to load an invalid survey: " + uri);
		}
		return surveyWork;
	}

	protected CollectSurvey loadSelectedSurvey() {
		String uri = selectedSurvey.getUri();
		CollectSurvey survey;
		if (selectedSurvey.isWork()) {
			survey = surveyManager.loadSurveyWork(selectedSurvey.getId());
		} else {
			survey = surveyManager.getByUri(uri);
		}
		return survey;
	}

	public ListModel<SurveySummary> getSurveySummaries() {
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
		return this.selectedSurvey == null || !this.selectedSurvey.isWork();
	}
	
}
