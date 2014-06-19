/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.Collect;
import org.openforis.collect.designer.form.validator.FormValidator;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.io.AbstractSurveyRestoreJob;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.SurveyBackupInfoExtractorJob;
import org.openforis.collect.io.SurveyRestoreJob;
import org.openforis.collect.io.XMLSurveyRestoreJob;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.concurrency.Job;
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

	private static final String XML_CONTENT = "text/xml";
	private static final String ZIP_CONTENT = "application/zip";
	private static final String ZIP_CONTENT_COMPRESSED = "application/x-zip-compressed";
	private static final String[] ALLOWED_UPLOAD_FILE_CONTENT = new String[] {ZIP_CONTENT, ZIP_CONTENT_COMPRESSED, XML_CONTENT};
	
	private static final String SURVEY_NAME_FIELD = "surveyName";
	
	private static final Log log = LogFactory.getLog(SurveyImportVM.class);
	
	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private SurveyValidator surveyValidator;
	@WireVariable
	private JobManager jobManager;

	private Map<String,String> form;
	
	private String uploadedSurveyUri;
	private File uploadedFile;
	private String uploadedFileName;
	private boolean uploadingXML;
	private boolean updatingExistingSurvey;
	private boolean updatingPublishedSurvey;

	//private SurveyUnmarshallProcess unmarshallProcess;
	//private SurveyImportProcess importProcess;
	private SurveyBackupInfoExtractorJob summaryJob;
	private AbstractSurveyRestoreJob restoreJob;
	
	private Window jobStatusPopUp;
	
	public SurveyImportVM() {
		form = new HashMap<String, String>();
		reset();
	}
	
	protected void reset() {
		if ( uploadedFile != null ) {
			uploadedFile.delete();
			uploadedFile = null;
		}
		uploadedFileName = null;
		uploadedSurveyUri = null;
		updatingExistingSurvey = false;
		updatingPublishedSurvey = false;
		updateForm();
		notifyChange("uploadedFileName","uploadedSurveyUri");
	}
	
	@Command
	public void importSurvey(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if ( validateForm(ctx) ) {
			if ( updatingExistingSurvey ) {
				Object[] args = new String[] {getFormSurveyName()};
				String messageKey = updatingPublishedSurvey ? 
					"survey.import_survey.confirm_overwrite_published.message": 
					"survey.import_survey.confirm_overwrite.message";
				
				String okLabelKey = updatingPublishedSurvey ? "survey.import_survey": "global.overwrite";
				
				MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
					@Override
					public void onOk() {
						startSurveyImport();
					}
				}, messageKey, args, 
					"survey.import_survey.confirm_overwrite.title", (String[]) null, 
					okLabelKey, "global.cancel");
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
		if ( ArrayUtils.contains(ALLOWED_UPLOAD_FILE_CONTENT, contentType) ) {
			File tempFile;
			this.uploadingXML = XML_CONTENT.equals(contentType);
			if ( uploadingXML ) {
				tempFile = OpenForisIOUtils.copyToTempFile(media.getReaderData(), "xml");
			} else {
				tempFile = OpenForisIOUtils.copyToTempFile(media.getStreamData(), "zip");
			}
			this.uploadedFile = tempFile;
			this.uploadedFileName = media.getName();
			notifyChange("uploadedFileName");
			updateForm();

			prepareSurveyImport(uploadingXML);
		} else {
			log.warn("Trying to upload invalid survey backup file. Content type: " + contentType);
			MessageUtil.showError("survey.import_survey.error_file_type_not_supported");
		}
	}

	protected void prepareSurveyImport(boolean validate) {
		if ( summaryJob != null && summaryJob.isRunning() ) {
			summaryJob.abort();
		}
		summaryJob = jobManager.createJob(SurveyBackupInfoExtractorJob.class);
		summaryJob.setFile(this.uploadedFile);
		summaryJob.setValidate(validate);
		
		jobManager.start(summaryJob);
		
		openSummaryCreationStatusPopUp();
	}
	
	protected void openSummaryCreationStatusPopUp() {
		String message = Labels.getLabel("survey.import_survey.unmarshall_process_status_popup.message");
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(message, summaryJob, true);
	}
	
	protected void openSurveyRestoreStatusPopUp() {
		String message = Labels.getLabel("survey.import_survey.import_process_status_popup.message");
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(message, restoreJob, false);
	}
	
	protected void closeJobStatusPopUp() {
		closePopUp(jobStatusPopUp);
		jobStatusPopUp = null;
	}
	
	private void showImportError(String errorMessageKey) {
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
	
	@GlobalCommand
	public void jobCompleted(@BindingParam("job") Job job) {
		if ( job == summaryJob ) {
			closeJobStatusPopUp();
			onSummaryCreationComplete();
		} else if ( job == restoreJob ) {
			closeJobStatusPopUp();
			onSurveyImportComplete();
		}
	}

	@GlobalCommand
	public void jobFailed(@BindingParam("job") Job job) {
		if ( job == summaryJob ) {
			closeJobStatusPopUp();
			String errorMessageKey = summaryJob.getErrorMessage();
			if ( summaryJob.isValidate() && summaryJob.getCurrentTask() instanceof IdmlUnmarshallTask ) {
				confirmImportInvalidSurvey(errorMessageKey);
			} else {
				showImportError(errorMessageKey);
				summaryJob = null;
				reset();
			}
		} else if ( job == restoreJob ) {
			closeJobStatusPopUp();
			showImportError(restoreJob.getErrorMessage());
			restoreJob = null;
		}
	}

	@GlobalCommand
	public void jobAborted(@BindingParam("job") Job job) {
		if ( job == summaryJob ) {
			closeJobStatusPopUp();
			reset();
			summaryJob = null;
		} else if ( job == restoreJob ) {
			closeJobStatusPopUp();
			restoreJob = null;
		}
	}

	protected void onSummaryCreationComplete() {
		SurveyBackupInfo info = summaryJob.getInfo();
		
		if ( Collect.getVersion() != null && 
				Collect.getVersion().compareTo(info.getCollectVersion()) < 0 ) {
			MessageUtil.showError("survey.import_survey.error.outdated_system_version");
		} else {
			survey = summaryJob.getSurvey();
			uploadedSurveyUri = info.getSurveyUri();
			summaryJob = null;
			
			notifyChange("uploadedSurveyUri");
			updateForm();
		}
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
				reset();
			}
		}, "survey.import_survey.confirm_process_invalid_survey.message", args,
				"survey.import_survey.confirm_process_invalid_survey.title", (String[]) null,
				"survey.import_survey.force_import", "global.cancel");
	}
	
	protected void updateForm() {
		String surveyName = null;
		if ( uploadedSurveyUri != null ) {
			SurveySummary surveySummary = surveyManager.loadSummaryByUri(uploadedSurveyUri);
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
		}
		form.put(SURVEY_NAME_FIELD, surveyName);
		notifyChange("updatingPublishedSurvey","updatingExistingSurvey","form");
	}

	protected void startSurveyImport() {
		String surveyName = getFormSurveyName();
		
		if ( FilenameUtils.getExtension(uploadedFile.getName()).equalsIgnoreCase("xml") ) {
			restoreJob = jobManager.createJob(XMLSurveyRestoreJob.class);
		} else {
			restoreJob = jobManager.createJob(SurveyRestoreJob.class);
		}
		restoreJob.setFile(uploadedFile);
		restoreJob.setSurveyName(surveyName);
		restoreJob.setSurveyUri(uploadedSurveyUri);
		restoreJob.setRestoreIntoPublishedSurvey(false);
		restoreJob.setValidateSurvey(false);
		jobManager.start(restoreJob);
		openSurveyRestoreStatusPopUp();
	}
	
	protected void onSurveyImportComplete() {
		Object[] args = new String[]{getFormSurveyName()};
		MessageUtil.showInfo("survey.import_survey.successfully_imported", args);
		closeImportPopUp(true);
		restoreJob = null;
	}
	
	protected boolean validateSurvey(CollectSurvey survey) {
		SurveyValidationResults validationResults = surveyValidator.validate(survey);
		if ( validationResults.isOk() ) {
			return true;
		} else {
			openValidationResultsPopUp(validationResults);
			return false;
		}
	}
	
	protected boolean validateSurveyForPublishing(CollectSurvey survey, CollectSurvey oldPublishedSurvey) {
		SurveyValidationResults validationResults = surveyValidator.validateCompatibility(oldPublishedSurvey, survey);
		if ( validationResults.isOk() ) {
			return true;
		} else {
			openValidationResultsPopUp(validationResults);
			return false;
		}
	}

	protected void openValidationResultsPopUp(SurveyValidationResults validationResults) {
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
	
	public String getUploadedFileName() {
		return uploadedFileName;
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
