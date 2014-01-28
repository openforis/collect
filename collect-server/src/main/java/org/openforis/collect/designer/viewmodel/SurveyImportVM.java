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
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.process.SimpleProcess;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.utils.ExecutorServiceUtil;
import org.openforis.collect.utils.OpenForisIOUtils;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.util.logging.Log;
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
	private static final String TEXT_XML_CONTENT = "text/xml";

	private static final Log log = Log.lookup(SurveyImportVM.class);
	
	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private SurveyValidator surveyValidator;

	private Map<String,String> form;
	
	private String fileName;
	private String uploadedSurveyUri;
	private File uploadedFile;
	private String uploadedFileName;
	private boolean updatingExistingSurvey;
	private boolean updatingPublishedSurvey;

	private SurveyUnmarshallProcess unmarshallProcess;
	private SurveyImportProcess importProcess;
	private Window processStatusPopUp;
	
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
		
		if ( TEXT_XML_CONTENT.equals(contentType) ) {
			File tempFile = OpenForisIOUtils.copyToTempFile(media.getReaderData());
			this.uploadedFile = tempFile;
			this.uploadedFileName = media.getName();
			updateForm();
			prepareSurveyImport(true);
		} else {
			MessageUtil.showError("survey.import_survey.error_file_type_not_supported");
		}
	}

	protected void prepareSurveyImport(boolean validate) {
		if ( unmarshallProcess != null && unmarshallProcess.getStatus().isRunning() ) {
			unmarshallProcess.cancel();
		}
		unmarshallProcess = new SurveyUnmarshallProcess(surveyManager, this.uploadedFile, validate);
		unmarshallProcess.init();
		ExecutorServiceUtil.executeInCachedPool(unmarshallProcess);
		openSurveyUnmarshallStatusPopUp();
	}
	
	protected void openSurveyUnmarshallStatusPopUp() {
		processStatusPopUp = ProcessStatusPopUpVM.openPopUp(
				Labels.getLabel("survey.import_survey.unmarshall_process_status_popup.message"), 
				unmarshallProcess, true);
	}
	
	protected void openSurveyImportStatusPopUp() {
		processStatusPopUp = ProcessStatusPopUpVM.openPopUp(
				Labels.getLabel("survey.import_survey.import_process_status_popup.message"), 
				importProcess, false);
	}
	
	protected void closeProcessStatusPopUp() {
		closePopUp(processStatusPopUp);
		processStatusPopUp = null;
	}
	
	@GlobalCommand
	public void processComplete() {
		closeProcessStatusPopUp();

		if ( unmarshallProcess != null ) {
			afterSurveyUnmarshallComplete();
		} else if ( importProcess != null ) {
			afterSurveyImportCompleted();
		}
	}
	
	@GlobalCommand
	public void processCancelled() {
		closeProcessStatusPopUp();
		if ( unmarshallProcess != null ) {
			uploadedFileName = null;
			uploadedSurveyUri = null;
			uploadedFile = null;
		}
		resetAsyncProcesses();
		updateForm();
	}

	@GlobalCommand
	public void processError(@BindingParam("errorMessage") String errorMessage) {
		closeProcessStatusPopUp();
		
		if ( unmarshallProcess != null && unmarshallProcess.isValidate() ) {
			confirmImportInvalidSurvey(errorMessage);
		} else if (errorMessage != null ) {
			Object[] args = new String[]{errorMessage};
			MessageUtil.showError("survey.import_survey.error", args);
		}
		resetAsyncProcesses();
	}
	
	private void resetAsyncProcesses() {
		unmarshallProcess = null;
		importProcess = null;
	}

	protected void afterSurveyUnmarshallComplete() {
		survey = unmarshallProcess.getSurvey();
		uploadedSurveyUri = survey.getUri();
		unmarshallProcess = null;
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
				surveyName = FilenameUtils.removeExtension(uploadedFileName);
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
		importProcess = new SurveyImportProcess(surveyManager, uploadedFile, survey, surveyName, updatingExistingSurvey, updatingPublishedSurvey);
		importProcess.init();
		ExecutorServiceUtil.executeInCachedPool(importProcess);
		openSurveyImportStatusPopUp();
	}
	
	protected void afterSurveyImportCompleted() {
		Object[] args = new String[]{getFormSurveyName()};
		MessageUtil.showInfo("survey.import_survey.successfully_imported", args);
		closeImportPopUp(true);
		importProcess = null;
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
	
	static class SurveyUnmarshallProcess extends SimpleProcess {

		private File file;
		private boolean validate;
		private SurveyManager surveyManager;
		private CollectSurvey survey;

		public SurveyUnmarshallProcess(SurveyManager surveyManager, File file, boolean validate) {
			this.surveyManager = surveyManager;
			this.file = file;
			this.validate = validate;
		}

		@Override
		public void startProcessing() throws Exception {
			super.startProcessing();
			try {
				this.survey = this.surveyManager.unmarshalSurvey(file, validate, false);
			} catch(IdmlParseException e) {
				log.warning("Error unmarhalling survey: " + e.getMessage());
				this.status.error();
				this.status.setErrorMessage(e.getMessage());
			} catch (SurveyValidationException e) {
				log.warning("Error validating survey to import: " + e.getMessage());
				this.status.error();
				this.status.setErrorMessage(e.getMessage());
			}
		}
		
		public CollectSurvey getSurvey() {
			return survey;
		}
		
		public boolean isValidate() {
			return validate;
		}
	}
	
	static class SurveyImportProcess extends SimpleProcess {
		private CollectSurvey survey;
		private File file;
		private SurveyManager surveyManager;
		private boolean updatingExistingSurvey;
		private boolean updatingPublishedSurvey;
		private String name;

		public SurveyImportProcess(SurveyManager surveyManager, File file,
				CollectSurvey survey, String name,
				boolean updatingExistingSurvey, boolean updatingPublishedSurvey) {
			this.surveyManager = surveyManager;
			this.file = file;
			this.survey = survey;
			this.name = name;
			this.updatingExistingSurvey = updatingExistingSurvey;
			this.updatingPublishedSurvey = updatingPublishedSurvey;
		}

		@Override
		public void startProcessing() throws Exception {
			super.startProcessing();
			if ( updatingExistingSurvey ) {
				if ( updatingPublishedSurvey ) {
					surveyManager.importInPublishedWorkModel(survey.getUri(), file, false);
				} else {
					surveyManager.updateModel(file, false);
				}
			} else {
				surveyManager.importWorkModel(file, name, false);
			}
		}
	}
}
