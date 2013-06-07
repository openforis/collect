/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.validator.FormValidator;
import org.openforis.collect.designer.model.SurveyManagerUtil;
import org.openforis.collect.designer.model.SurveyWorkSummary;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.SurveyValidator;
import org.openforis.collect.manager.SurveyValidator.SurveyValidationResult;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.util.logging.Log;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;

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

	private Map<String,String> form;
	
	private String fileName;
	private CollectSurvey uploadedSurvey;
	private boolean updatingExistingSurvey;
	private boolean updatingPublishedSurvey;
	
	public SurveyImportVM() {
		form = new HashMap<String, String>();
		reset();
	}
	
	protected void reset() {
		fileName = null;
		uploadedSurvey = null;
		updatingExistingSurvey = false;
		updatingPublishedSurvey = false;
		notifyChange("fileName","uploadedSurvey","nameEditDisabled");
	}
	
	@Command
	public void importSurvey(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if ( validateForm(ctx) ) {
			final String name = getFormSurveyName();
			if ( updatingExistingSurvey ) {
				Object[] args = new String[] {name};
				String messageKey = updatingPublishedSurvey ? 
					"survey.import_survey.confirm_overwrite_published": 
					"survey.import_survey.confirm_overwrite";
				MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
					@Override
					public void onOk() {
						processSurveyImport(name, true);
					}
				}, messageKey, args);
			} else {
				processSurveyImport(name, false);
			}
		}
	}
	
	protected boolean validateForm(BindContext ctx) {
		String surveyName = getFormSurveyName();
		String messageKey = null;
		if (StringUtils.isBlank(surveyName ) ) {
			messageKey = "survey.import_survey.specify_name";
		} else if ( uploadedSurvey == null ) {
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
		List<SurveyWorkSummary> summaries = SurveyManagerUtil.getSurveySummaries(surveyManager);
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
		if ( contentType.equals(TEXT_XML_CONTENT) ) {
			unmarshalSurvey(media, false);
		} else {
			MessageUtil.showError("survey.import_survey.error_file_type_not_supported");
		}
	}

	protected void unmarshalSurvey(final Media media, boolean skipValidation) {
		try {
			Reader reader = media.getReaderData();
			uploadedSurvey = surveyManager.unmarshalSurvey(reader, ! skipValidation);
			if ( validateSurvey(uploadedSurvey) ) {
				updateForm(media.getName());
			} else {
				reset();
			}
		} catch(IdmlParseException e) {
			log.error("Error unmarhalling survey", e);
			Object[] args = new String[]{e.getMessage()};
			MessageUtil.showError("survey.import_survey.error", args);
		} catch (SurveyValidationException e) {
			Object[] args = new String[] {e.getMessage()};
			MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					unmarshalSurvey(media, true);
				}
			}, "survey.import_survey.confirm_process_invalid_survey", args);
		}
	}

	protected void updateForm(String uploadedFileName) {
		if ( uploadedSurvey == null ) {
			fileName = null;
		} else {
			fileName = uploadedFileName;
		}
		String uri = uploadedSurvey.getUri();
		CollectSurvey publishedSurvey = surveyManager.getByUri(uri);
		SurveySummary surveyWork = surveyManager.loadSurveyWorkSummaryByUri(uri);
		String surveyName = null;
		if ( publishedSurvey != null ) {
			updatingExistingSurvey = true;
			updatingPublishedSurvey = true;
			surveyName = publishedSurvey.getName();
		} else if ( surveyWork != null ) {
			updatingExistingSurvey = true;
			updatingPublishedSurvey = false;
			surveyName = surveyWork.getName();
		} else {
			updatingExistingSurvey = false;
			updatingPublishedSurvey = false;
			surveyName = getFormSurveyName();
			if ( StringUtils.isEmpty(surveyName) ) {
				surveyName = FilenameUtils.removeExtension(fileName);
			}
		}
		form.put(SURVEY_NAME_FIELD, surveyName);
		notifyChange("fileName","uploadedSurvey","updatingPublishedSurvey","updatingExistingSurvey","form");
	}

	protected void processSurveyImport(String surveyName, boolean overwrite) {
		uploadedSurvey.setName(surveyName);
		String uri = uploadedSurvey.getUri();
		SurveySummary oldSurveyWorkSummary = surveyManager.loadSurveyWorkSummaryByUri(uri);
		CollectSurvey oldPublishedSurvey = surveyManager.getByUri(uri);
		if ( (oldSurveyWorkSummary != null || oldPublishedSurvey != null) && ! overwrite ) {
			throw new IllegalArgumentException("Survey with same URI already exists and override parameter not specified");
		}
		try {
			if ( oldSurveyWorkSummary != null ) {
				uploadedSurvey.setId(oldSurveyWorkSummary.getId());
				surveyManager.saveSurveyWork(uploadedSurvey);
			} else if ( oldPublishedSurvey != null && validateSurveyForPublishing(uploadedSurvey, oldPublishedSurvey)) {
				uploadedSurvey.setId(oldPublishedSurvey.getId());
				surveyManager.updateModel(uploadedSurvey);
			} else {
				surveyManager.saveSurveyWork(uploadedSurvey);
			}
			closeImportPopUp(true);
			Object[] args = new String[]{surveyName};
			MessageUtil.showInfo("survey.import_survey.successfully_imported", args);
		} catch (SurveyImportException e) {
			log.error(e);
			Object[] args = new String[]{e.getMessage()};
			MessageUtil.showError("survey.import_survey.error", args);
		}
	}
	
	protected boolean validateSurvey(CollectSurvey survey) {
		SurveyValidator surveyValidator = new SurveyValidator(surveyManager);
		List<SurveyValidationResult> validationResults = surveyValidator.validate(survey);
		if ( validationResults.isEmpty() ) {
			return true;
		} else {
			openValidationResultsPopUp(validationResults);
			return false;
		}
	}
	
	protected boolean validateSurveyForPublishing(CollectSurvey survey, CollectSurvey oldPublishedSurvey) {
		SurveyValidator surveyValidator = new SurveyValidator(surveyManager);
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
	
	public CollectSurvey getUploadedSurvey() {
		return uploadedSurvey;
	}
	
	public Map<String, String> getForm() {
		return form;
	}
	
	public void setForm(Map<String, String> form) {
		this.form = form;
	}
}
