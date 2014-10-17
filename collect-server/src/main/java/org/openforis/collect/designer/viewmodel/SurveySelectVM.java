/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
import org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.SurveyType;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyBackupJob.OutputFormat;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.relational.data.RecordIterator;
import org.openforis.collect.relational.print.RDBPrintJob;
import org.openforis.collect.utils.Dates;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.spring.SpringJobManager;
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

	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private RecordManager recordManager;
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

	private RDBPrintJob rdbExportJob;

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
		rdbExportJob = null;
		surveyBackupJob = null;
		
		String uri = selectedSurvey.getUri();
		CollectSurvey survey;
		if ( selectedSurvey.isWork() && SurveyType.valueOf(parameters.getType()) == SurveyType.TEMPORARY ) {
			survey = surveyManager.loadSurveyWork(selectedSurvey.getId());
		} else {
			survey = surveyManager.getByUri(uri);
		}
		Integer surveyId = survey.getId();
		String surveyName = survey.getName();
		
		Job job;
		switch(parameters.getOutputFormatEnum()) {
		case RDB:
			rdbExportJob = new RDBPrintJob();
			rdbExportJob.setSurvey(survey);
			rdbExportJob.setTargetSchemaName(survey.getName());
			rdbExportJob.setRecordIterator(new RecordManagerRecordIterator(survey, Step.ANALYSIS));
			rdbExportJob.setIncludeData(parameters.isIncludeData());
			rdbExportJob.setDialect(parameters.getRdbDialectEnum());
			rdbExportJob.setDateTimeFormat(parameters.getRdbDateTimeFormat());
			rdbExportJob.setTargetSchemaName(parameters.getRdbTargetSchemaName());
			job = rdbExportJob;
			break;
		default:
			surveyBackupJob = springJobManager.createJob(SurveyBackupJob.class);
			surveyBackupJob.setSurvey(survey);
			surveyBackupJob.setIncludeData(parameters.isIncludeData());
			surveyBackupJob.setIncludeRecordFiles(parameters.isIncludeUploadedFiles());
			surveyBackupJob.setOutputFormat(OutputFormat.valueOf(parameters.getOutputFormat()));
			job = surveyBackupJob;
			break;
		}
		springJobManager.start(job, String.valueOf(surveyId));

		closePopUp(surveyExportPopup);
		surveyExportPopup = null;
		
		openSurveyExportStatusPopUp(surveyName, job);
	}

	protected void openSurveyExportStatusPopUp(String surveyName, Job job) {
		String title = Labels.getLabel("survey.export_survey.process_status_popup.message", new String[] { surveyName });
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(title, job, true);
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
			File file = surveyBackupJob.getOutputFile();
			CollectSurvey survey = surveyBackupJob.getSurvey();
			String extension = surveyBackupJob.getOutputFormat().getOutputFileExtension();
			downloadFile(file, survey, extension, BINARY_CONTENT_TYPE);
			surveyBackupJob = null;
		} else if ( job == rdbExportJob ) {
			File file = rdbExportJob.getOutputFile();
			CollectSurvey survey = rdbExportJob.getSurvey();
			String extension = "sql";
			downloadFile(file, survey, extension, "test/plain");
			rdbExportJob = null;
		}
	}
	
	private void downloadFile(File file, CollectSurvey survey, String extension, String contentType) {
		String surveyName = survey.getName();
		String dateStr = Dates.formatDateTime(new Date());
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
		if ( validationResultsPopUp != null ) {
			closePopUp(validationResultsPopUp);
			validationResultsPopUp = null;
			CollectSurvey survey = loadSelectedSurvey();
			performSurveyPublishing(survey);
		}
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
				SurveySummary oldSummary = findSummary(newSummary.getId(), newSummary.isPublished(), newSummary.isWork());
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

	private SurveySummary findSummary(Integer id, boolean published, boolean work) {
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
			CollectRecord record = recordManager.load(survey, summary.getId());
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
}
