/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmParams;
import org.openforis.collect.designer.util.PageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.designer.util.SuccessHandler;
import org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject;
import org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.SurveyType;
import org.openforis.collect.designer.viewmodel.SurveyValidationResultsVM.ConfirmEvent;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyBackupJob.OutputFormat;
import org.openforis.collect.io.data.DataBackupError;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreator;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreatorImpl;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.relational.data.RecordIterator;
import org.openforis.collect.relational.print.RDBPrintJob;
import org.openforis.collect.utils.Dates;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.EntityDefinition;
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
import org.zkoss.zk.ui.event.EventListener;
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

	private static final String BINARY_CONTENT_TYPE = "application/octet-stream";

	/**
	 * Pattern for survey export file name (SURVEYNAME_DATE.OUTPUTFORMAT)
	 */
	private static final String SURVEY_EXPORT_FILE_NAME_PATTERN = "%s_%s.%s";

	private static Log log = Log.lookup(SurveySelectVM.class);

	public static final String CLOSE_SURVEY_IMPORT_POP_UP_GLOBAL_COMMNAD = "closeSurveyImportPopUp";

	public static final String UPDATE_SURVEY_LIST_COMMAND = "updateSurveyList";

	private static final String COLLECT_EARTH_PROJECT_FILE_EXTENSION = "cep";

	private static final CollectEarthProjectFileCreator COLLECT_EARTH_PROJECT_FILE_CREATOR;

	static {
		Iterator<CollectEarthProjectFileCreator> it = COLLECT_EARTH_PROJECT_FILE_CREATOR_LOADER.iterator();
		COLLECT_EARTH_PROJECT_FILE_CREATOR = it.hasNext() ? it.next(): null;
	}
	
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

	private Window surveyExportPopup;

	private Window surveyClonePopup;

	private SurveySummary selectedSurvey;

	private List<SurveySummary> summaries;

	private SurveyBackupJob surveyBackupJob;

	private RDBPrintJob rdbExportJob;

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
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("survey", selectedSurvey);
		surveyExportPopup = openPopUp(Resources.Component.SURVEY_EXPORT_PARAMETERS_POPUP.getLocation(), true, args);
	}

	@GlobalCommand
	public void performSelectedSurveyExport(@BindingParam("parameters") final SurveyExportParametersFormObject parameters) {
		rdbExportJob = null;
		surveyBackupJob = null;
		
		String uri = selectedSurvey.getUri();
		final CollectSurvey survey;
		if ( selectedSurvey.isTemporary() && SurveyType.valueOf(parameters.getType()) == SurveyType.TEMPORARY ) {
			survey = surveyManager.loadSurvey(selectedSurvey.getId());
		} else {
			survey = surveyManager.getByUri(uri);
		}
		switch(parameters.getOutputFormatEnum()) {
		case EARTH:
			validateSurvey(survey, collectEarthSurveyValidator, new SuccessHandler() {
				public void onSuccess() {
					exportCollectEarthSurvey(survey, parameters);
				}
			}, true);
			return;
		case RDB:
			startRDBSurveyExportJob(survey, parameters);
			break;
		case MOBILE:
			validateSurvey(survey, surveyValidator, new SuccessHandler() {
				public void onSuccess() {
					startCollectSurveyExportJob(survey, parameters);
				}
			}, true);
			break;
		default:
			startCollectSurveyExportJob(survey, parameters);
			break;
		}
		closePopUp(surveyExportPopup);
		surveyExportPopup = null;
	}

	private void startRDBSurveyExportJob(final CollectSurvey survey,
			final SurveyExportParametersFormObject parameters) {
		rdbExportJob = new RDBPrintJob();
		rdbExportJob.setSurvey(survey);
		rdbExportJob.setTargetSchemaName(survey.getName());
		rdbExportJob.setRecordIterator(new RecordManagerRecordIterator(survey, Step.ANALYSIS));
		rdbExportJob.setIncludeData(parameters.isIncludeData());
		rdbExportJob.setDialect(parameters.getRdbDialectEnum());
		rdbExportJob.setDateTimeFormat(parameters.getRdbDateTimeFormat());
		rdbExportJob.setTargetSchemaName(parameters.getRdbTargetSchemaName());
		jobManager.start(rdbExportJob, String.valueOf(survey.getId()));
		openSurveyExportStatusPopUp(survey.getName(), rdbExportJob);
	}

	private void exportCollectEarthSurvey(final CollectSurvey survey,
			final SurveyExportParametersFormObject parameters) {
		try {
			((CollectEarthProjectFileCreatorImpl) COLLECT_EARTH_PROJECT_FILE_CREATOR).setCodeListManager(codeListManager);
			String languageCode = parameters.getLanguageCode();
			File file = COLLECT_EARTH_PROJECT_FILE_CREATOR.create(survey, languageCode);
			String contentType = URLConnection.guessContentTypeFromName(file.getName());
			FileInputStream is = new FileInputStream(file);
			String outputFileName = String.format("%s_%s_%s.%s", 
					survey.getName(), 
					languageCode,
					Dates.formatLocalDateTime(survey.getModifiedDate()),
					COLLECT_EARTH_PROJECT_FILE_EXTENSION);
			Filedownload.save(is, contentType, outputFileName);
		} catch(Exception e) {
			log.error(e);
			MessageUtil.showError("survey.export.error_generating_collect_earth_project_file", new String[] {e.getMessage()});
		}
	}

	protected void startCollectSurveyExportJob(CollectSurvey survey,
			SurveyExportParametersFormObject parameters) {
		surveyBackupJob = jobManager.createJob(SurveyBackupJob.class);
		surveyBackupJob.setSurvey(survey);
		surveyBackupJob.setIncludeData(parameters.isIncludeData());
		surveyBackupJob.setIncludeRecordFiles(parameters.isIncludeUploadedFiles());
		surveyBackupJob.setOutputFormat(OutputFormat.valueOf(parameters.getOutputFormat()));
		jobManager.start(surveyBackupJob, String.valueOf(survey.getId()));
		openSurveyExportStatusPopUp(survey.getName(), surveyBackupJob);
	}

	protected void openSurveyExportStatusPopUp(String surveyName, Job job) {
		String title = Labels.getLabel("survey.export_survey.process_status_popup.message", new String[] { surveyName });
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(title, job, true);
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
		if (job == surveyBackupJob) {
			surveyBackupJob = null;
		} else if (job == surveyCloneJob) {
			surveyCloneJob = null;
		} else if (job == rdbExportJob) {
			rdbExportJob = null;
		}
		closeJobStatusPopUp();
	}

	private boolean isJobStartedByThis(Job job) {
		return job == surveyBackupJob || job == surveyCloneJob || job == rdbExportJob;
	}
	
	@GlobalCommand
	public void jobCompleted(@BindingParam("job") Job job) {
		boolean jobStartedByThis = isJobStartedByThis(job);
		if ( job == surveyBackupJob ) {
			File file = surveyBackupJob.getOutputFile();
			CollectSurvey survey = surveyBackupJob.getSurvey();
			String extension = surveyBackupJob.getOutputFormat().getOutputFileExtension();
			downloadFile(file, survey, extension, BINARY_CONTENT_TYPE);
			final List<DataBackupError> dataBackupErrors = surveyBackupJob.getDataBackupErrors();
			if (! dataBackupErrors.isEmpty()) {
				DataExportErrorsPopUpVM.showPopUp(dataBackupErrors);
			}
			surveyBackupJob = null;
		} else if ( job == rdbExportJob ) {
			File file = rdbExportJob.getOutputFile();
			CollectSurvey survey = rdbExportJob.getSurvey();
			String extension = "sql";
			downloadFile(file, survey, extension, "test/plain");
			rdbExportJob = null;
		} else if (job == surveyCloneJob) {
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
	
	private void downloadFile(File file, CollectSurvey survey, String extension, String contentType) {
		String surveyName = survey.getName();
		String dateStr = Dates.formatLocalDateTime(new Date());
		String fileName = String.format(SURVEY_EXPORT_FILE_NAME_PATTERN, surveyName, dateStr, extension);
		try {
			Filedownload.save(new FileInputStream(file), contentType, fileName);
		} catch (FileNotFoundException e) {
			log.error(e);
			MessageUtil.showError("survey.export_survey.error", new String[]{e.getMessage()});
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

	private void validateSurvey(CollectSurvey survey, SurveyValidator validator, final SuccessHandler successHandler, boolean showWarningConfirm) {
		SurveyValidationResults validationResults = validator.validate(survey);
		if (validationResults.isOk()) {
			successHandler.onSuccess();
		} else {
			final Window validationResultsPopUp = SurveyValidationResultsVM.showPopUp(validationResults, showWarningConfirm && ! validationResults.hasErrors());
			validationResultsPopUp.addEventListener(SurveyValidationResultsVM.CONFIRM_EVENT_NAME, new EventListener<ConfirmEvent>() {
				public void onEvent(ConfirmEvent event) throws Exception {
					successHandler.onSuccess();
					closePopUp(validationResultsPopUp);
				}
			});
		}
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

	private class RecordManagerRecordIterator implements RecordIterator {
		
		private List<CollectRecord> summaries;
		private int nextRecordIndex = 0;
		private CollectSurvey survey;
		
		public RecordManagerRecordIterator(CollectSurvey survey, Step step) {
			this.survey = survey;
			this.summaries = new ArrayList<CollectRecord>();
			for (EntityDefinition rootDef : survey.getSchema().getRootEntityDefinitions()) {
				this.summaries.addAll(recordManager.loadSummaries(survey, rootDef.getName(), step));
			}
		}
		
		@Override
		public boolean hasNext() {
			return nextRecordIndex < size();
		}

		@Override
		public CollectRecord next() {
			CollectRecord summary = summaries.get(nextRecordIndex++);
			CollectRecord record = recordManager.load(survey, summary.getId(), summary.getStep());
			return record;
		}

		@Override
		public void remove() {
		}

		@Override
		public int size() {
			return summaries.size();
		}
		
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
