/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.validator.FormValidator;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.io.metadata.SurveyRestoreJob;
import org.openforis.collect.io.metadata.SurveyUnmarshallJob;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.utils.OpenForisIOUtils;
import org.openforis.concurrency.JobManager;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class SurveyImportVM extends SurveyBaseVM {

	private static final String SURVEY_NAME_FIELD = "surveyName";
//	private static final String TEXT_XML_CONTENT = "text/xml";
	private static final Object ZIP_CONTENT = "application/zip";

	//private static final Log log = Log.lookup(SurveyImportVM.class);
	
	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private SurveyValidator surveyValidator;
	@WireVariable
	private JobManager jobManager;

	private Map<String,String> form;
	
	private String fileName;
	private String uploadedSurveyUri;
	private File uploadedFile;
	private String uploadedFileName;
	private boolean updatingExistingSurvey;
	private boolean updatingPublishedSurvey;

	//private SurveyUnmarshallProcess unmarshallProcess;
	//private SurveyImportProcess importProcess;
	private SurveyUnmarshallJob unmarshallJob;
	private SurveyRestoreJob restoreJob;
	
	private Window jobStatusPopUp;
	
	public SurveyImportVM() {
		form = new HashMap<String, String>();
		reset();
	}
	
	protected void reset() {
		fileName = null;
		if ( uploadedFile != null ) {
			uploadedFile.delete();
			uploadedFile = null;
		}
		uploadedSurveyUri = null;
		updatingExistingSurvey = false;
		updatingPublishedSurvey = false;
		notifyChange("fileName","uploadedSurveyUri","updatingPublishedSurvey","updatingExistingSurvey","form");
	}
	
	@Command
	public void importSurvey(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if ( validateForm(ctx) ) {
			if ( updatingExistingSurvey ) {
				Object[] args = new String[] {getFormSurveyName()};
				String messageKey = updatingPublishedSurvey ? 
					"survey.import_survey.confirm_overwrite_published.message": 
					"survey.import_survey.confirm_overwrite.message";
				MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
					@Override
					public void onOk() {
						startSurveyImport();
					}
				}, messageKey, args, 
					"survey.import_survey.confirm_overwrite.title", (String[]) null, 
					"global.overwrite", "global.cancel");
			} else {
				startSurveyImport();
			}
		}
	}
	
	protected boolean validateForm(BindContext ctx) {
		String surveyName = getFormSurveyName();
		String messageKey = null;
		if (StringUtils.isBlank(surveyName ) ) {
			messageKey = "survey.import_survey.specify_name";
		} else if ( uploadedFile == null ) {
			messageKey = "survey.import_survey.upload_a_file";
		} else if ( ! updatingExistingSurvey && existsSurveyWithName(surveyName) ) {
			messageKey = "survey.import_survey.error.duplicate_name";
		}
		if ( messageKey == null ) {
			return true;
		} else {
			MessageUtil.showWarning(messageKey);
			return false;
		}
	}
	
	protected boolean existsSurveyWithName(String name) {
		List<SurveySummary> summaries = surveyManager.loadSummaries();
		for (SurveySummary summary : summaries) {
			String summaryName = summary.getName();
			if ( summaryName.equals(name) ) {
				return true;
			}
		}
		return false;
	}

	private String getFormSurveyName() {
		return (String) form.get(SURVEY_NAME_FIELD);
	}
	
	public Validator getNameValidator() {
		return new FormValidator() {
			@Override
			protected void internalValidate(ValidationContext ctx) {
				boolean result = validateRequired(ctx, SURVEY_NAME_FIELD);
				if ( result ) {
					result = validateInternalName(ctx, SURVEY_NAME_FIELD);
				}
			}
		};
	}

	@Command
	public void fileUploaded(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
 		Media media = event.getMedia();
		String contentType = media.getContentType();
		
		if ( ZIP_CONTENT.equals(contentType) ) {
			File tempFile = OpenForisIOUtils.copyToTempFile(media.getStreamData());
			this.uploadedFile = tempFile;
			this.uploadedFileName = media.getName();
			updateForm();
			prepareSurveyImport(true);
		} else {
			MessageUtil.showError("survey.import_survey.error_file_type_not_supported");
		}
	}

	protected void prepareSurveyImport(boolean validate) {
		if ( unmarshallJob != null && unmarshallJob.isRunning() ) {
			unmarshallJob.abort();
		}
		unmarshallJob = jobManager.createJob(SurveyUnmarshallJob.class);
		unmarshallJob.setFile(this.uploadedFile);
		unmarshallJob.setValidate(validate);
		jobManager.start(unmarshallJob);
		openSurveyUnmarshallStatusPopUp();
	}
	
	protected void openSurveyUnmarshallStatusPopUp() {
		String message = Labels.getLabel("survey.import_survey.unmarshall_process_status_popup.message");
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(message, unmarshallJob, true);
	}
	
	protected void openSurveyRestoreStatusPopUp() {
		String message = Labels.getLabel("survey.import_survey.import_process_status_popup.message");
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(message, restoreJob, false);
	}
	
	protected void closeJobStatusPopUp() {
		closePopUp(jobStatusPopUp);
		jobStatusPopUp = null;
	}
	
	@GlobalCommand
	public void jobCompleted() {
		closeJobStatusPopUp();

		if ( unmarshallJob != null ) {
			afterSurveyUnmarshallComplete();
		} else if ( restoreJob!= null ) {
			afterSurveyImportCompleted();
		}
	}
	
	@GlobalCommand
	public void jobAborted() {
		closeJobStatusPopUp();
		if ( unmarshallJob != null ) {
			uploadedFileName = null;
			uploadedSurveyUri = null;
			uploadedFile = null;
		}
		resetAsyncProcesses();
		updateForm();
	}

	@GlobalCommand
	public void jobFailed(@BindingParam("errorMessage") String errorMessageKey) {
		closeJobStatusPopUp();
		
		if ( unmarshallJob != null && unmarshallJob.isValidate() ) {
			confirmImportInvalidSurvey(errorMessageKey);
		} else {
			String message = null;
			if ( errorMessageKey == null ) {
				message = null;
			} else {
				//try to get message using labels repository
				String labelsMessage = Labels.getLabel(errorMessageKey);
				message = labelsMessage == null ? errorMessageKey: labelsMessage;
			}
			Object[] args = new String[] { message };
			MessageUtil.showError("survey.import_survey.error", args);
		}
		resetAsyncProcesses();
	}
	
	private void resetAsyncProcesses() {
		unmarshallJob = null;
		restoreJob = null;
	}

	protected void afterSurveyUnmarshallComplete() {
		survey = unmarshallJob.getSurvey();
		uploadedSurveyUri = survey.getUri();
		unmarshallJob = null;
		updateForm();
	}

	protected void confirmImportInvalidSurvey(String errorMessage) {
		Object[] args = new String[] {errorMessage};
		MessageUtil.showConfirm(new MessageUtil.CompleteConfirmHandler() {
			@Override
			public void onOk() {
				prepareSurveyImport(false);
			}
			@Override
			public void onCancel() {
				uploadedFile = null;
				uploadedFileName = null;
				uploadedSurveyUri = null;
				updateForm();
			}
		}, "survey.import_survey.confirm_process_invalid_survey.message", args,
				"survey.import_survey.confirm_process_invalid_survey.title", (String[]) null,
				"survey.import_survey.force_import", "global.cancel");
	}
	
	protected void updateForm() {
		SurveySummary surveySummary = surveyManager.loadSummaryByUri(uploadedSurveyUri);
		String surveyName = null;
		if ( surveySummary == null ) {
			updatingExistingSurvey = false;
			surveyName = getFormSurveyName();
			updatingPublishedSurvey = false;
			if ( StringUtils.isEmpty(surveyName) ) {
				surveyName = suggestSurveyName(uploadedFileName);
			}
		} else {
			updatingExistingSurvey = true;
			updatingPublishedSurvey = ! surveySummary.isWork();
			surveyName = surveySummary.getName();
		}
		this.fileName = uploadedFileName;
		form.put(SURVEY_NAME_FIELD, surveyName);
		notifyChange("fileName","uploadedSurveyUri","updatingPublishedSurvey","updatingExistingSurvey","form");
	}

	protected void startSurveyImport() {
		String surveyName = getFormSurveyName();
		restoreJob = jobManager.createJob(SurveyRestoreJob.class);
		restoreJob.setFile(uploadedFile);
		restoreJob.setPublishedSurveyUri(survey.getUri());
		restoreJob.setSurveyName(surveyName);
		restoreJob.setUpdatingExistingSurvey(updatingExistingSurvey);
		restoreJob.setUpdatingPublishedSurvey(updatingPublishedSurvey);

		jobManager.start(restoreJob);
		openSurveyRestoreStatusPopUp();
	}
	
	protected void afterSurveyImportCompleted() {
		Object[] args = new String[]{getFormSurveyName()};
		MessageUtil.showInfo("survey.import_survey.successfully_imported", args);
		closeImportPopUp(true);
		restoreJob = null;
	}
	
	protected boolean validateSurvey(CollectSurvey survey) {
		List<SurveyValidationResult> validationResults = surveyValidator.validate(survey);
		if ( validationResults.isEmpty() ) {
			return true;
		} else {
			openValidationResultsPopUp(validationResults);
			return false;
		}
	}
	
	protected boolean validateSurveyForPublishing(CollectSurvey survey, CollectSurvey oldPublishedSurvey) {
		List<SurveyValidationResult> validationResults = surveyValidator.validateCompatibility(oldPublishedSurvey, survey);
		if ( validationResults.isEmpty() ) {
			return true;
		} else {
			openValidationResultsPopUp(validationResults);
			return false;
		}
	}

	protected void openValidationResultsPopUp(List<SurveyValidationResult> validationResults) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("validationResults", validationResults);
		BindUtils.postGlobalCommand(null, null, "openValidationResultsPopUp", args);
	}

	protected void closeImportPopUp(boolean successfullyImported) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("successfullyImported", successfullyImported);
		BindUtils.postGlobalCommand(null, null, SurveySelectVM.CLOSE_SURVEY_IMPORT_POP_UP_GLOBAL_COMMNAD, args);
	}
	
	private String suggestSurveyName(String fileName) {
		//remove extension
		String result = FilenameUtils.removeExtension(fileName);
		//make it all lowercase
		result = result.toLowerCase();
		//replace invalid characters with underscore character (_)
		result = result.replaceAll("[^0-9a-z_]", "_");
		//remove trailing underscore character
		result = result.replaceAll("^_+", "");
		return result;
	}
	
	public boolean isUpdatingPublishedSurvey() {
		return updatingPublishedSurvey;
	}
	
	public boolean isUpdatingExistingSurvey() {
		return updatingExistingSurvey;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getUploadedSurveyUri() {
		return uploadedSurveyUri;
	}
	
	public Map<String, String> getForm() {
		return form;
	}
	
	public void setForm(Map<String, String> form) {
		this.form = form;
	}
	
}
