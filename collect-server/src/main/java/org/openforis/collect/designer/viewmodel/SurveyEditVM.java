/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.model.LabelKeys;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.PageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.io.metadata.SchemaSummaryCSVExportJob;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.utils.Dates;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.spring.SpringJobManager;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.QueryParam;
import org.zkoss.util.logging.Log;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class SurveyEditVM extends SurveyBaseVM {

	private static final Log log = Log.lookup(SurveySelectVM.class);

	private static final String TEXT_XML = "text/xml";
	public static final String SHOW_PREVIEW_POP_UP_GLOBAL_COMMAND = "showPreview";
	public static final String BACKGROUD_SAVE_GLOBAL_COMMAND = "backgroundSurveySave";
	private static final String CODE_LISTS_POP_UP_CLOSED_COMMAND = "codeListsPopUpClosed";
	
	private static final String SCHEMA_SUMMARY_FILE_NAME_PATTERN = "%s_schema_summary_%s.%s";
	
	private Window selectLanguagePopUp;
	private Window previewPreferencesPopUp;
	private Window srsPopUp;
	private Window codeListsPopUp;
	private Window unitsPopUp;
	private Window versioningPopUp;

	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private SurveyValidator surveyValidator;
	@WireVariable(value="springJobManager")
	private SpringJobManager jobManager;

	private boolean changed;
	private Window validationResultsPopUp;
	private Window jobStatusPopUp;

	private boolean showingPreview;

	@Init(superclass=false)
	public void init(@QueryParam("temp_id") Integer tempId) {
		super.init();
		if ( survey == null ) {
			backToSurveysList();
		} else {
			changed = false;
			currentLanguageCode = survey.getDefaultLanguage();
			if ( currentLanguageCode == null ) {
				openLanguageManagerPopUp();
			} else {
				SessionStatus sessionStatus = getSessionStatus();
				sessionStatus.setCurrentLanguageCode(currentLanguageCode);
			}
			String confirmCloseMessage = Labels.getLabel("survey.edit.leave_page");
			PageUtil.confirmClose(confirmCloseMessage);
		}
	}

	@Command
	public void openLanguageManagerPopUp() {
		if ( checkCanLeaveForm() ) {
			selectLanguagePopUp = openPopUp(Resources.Component.SELECT_LANGUAGE_POP_UP.getLocation(), true);
		}
	}
	
	@GlobalCommand
	public void exportSurvey() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		surveyManager.marshalSurvey(survey, os);
		byte[] content = os.toByteArray();
		String fileName = survey.getName() + ".xml";
		Filedownload.save(content, TEXT_XML, fileName);
	}
	
	@GlobalCommand
	public void openSRSManagerPopUp() {
		if ( checkCanLeaveForm() ) {
			srsPopUp = openPopUp(Resources.Component.SRS_MANAGER_POP_UP.getLocation(), true);
		}
	}
	
	@GlobalCommand
	public void closeSRSManagerPopUp() {
		closePopUp(srsPopUp);
		srsPopUp = null;
		dispatchCurrentFormValidatedCommand(true);
	}
	
	@GlobalCommand
	public void openCodeListsManagerPopUp(
			@BindingParam(CodeListsVM.EDITING_ATTRIBUTE_PARAM) Boolean editingAttribute, 
			@BindingParam(CodeListsVM.SELECTED_CODE_LIST_PARAM) CodeList selectedCodeList) {
		if ( codeListsPopUp == null ) { 
			if (selectedCodeList == survey.getSamplingDesignCodeList()) {
				MessageUtil.showWarning("survey.code_list.alert.cannot_edit_sampling_design_list");
			} else {
				dispatchCurrentFormValidatedCommand(true);
				Map<String, Object> args = new HashMap<String, Object>();
				args.put(CodeListsVM.EDITING_ATTRIBUTE_PARAM, editingAttribute);
				args.put(CodeListsVM.SELECTED_CODE_LIST_PARAM, selectedCodeList);
				codeListsPopUp = openPopUp(Resources.Component.CODE_LISTS_POPUP.getLocation(), true, args);
			}
		}
	}

	@GlobalCommand
	public void closeCodeListsManagerPopUp(@ContextParam(ContextType.BINDER) Binder binder,
			@BindingParam(CodeListsVM.EDITING_ATTRIBUTE_PARAM) final Boolean editingAttribute,
			@BindingParam(CodeListsVM.SELECTED_CODE_LIST_PARAM) final CodeList selectedCodeList) {
		if ( codeListsPopUp != null ) {
			closePopUp(codeListsPopUp);
			codeListsPopUp = null;
			dispatchCurrentFormValidatedCommand(true);
			dispatchCodeListsPopUpClosedCommand(editingAttribute, selectedCodeList);
		}
	}

	public void dispatchCodeListsPopUpClosedCommand(Boolean editingAttribute, CodeList selectedCodeList) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(CodeListsVM.EDITING_ATTRIBUTE_PARAM, editingAttribute);
		args.put(CodeListsVM.SELECTED_CODE_LIST_PARAM, selectedCodeList);
		BindUtils.postGlobalCommand(null, null, CODE_LISTS_POP_UP_CLOSED_COMMAND, args);
	}
	
	@GlobalCommand
	public void openUnitsManagerPopUp() {
		if ( unitsPopUp == null ) {
			dispatchCurrentFormValidatedCommand(true);
			unitsPopUp = openPopUp(Resources.Component.UNITS_MANAGER_POP_UP.getLocation(), true);
		}
	}
	
	@GlobalCommand
	public void closeUnitsManagerPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( unitsPopUp != null ) {
			closePopUp(unitsPopUp);
			unitsPopUp = null;
			dispatchCurrentFormValidatedCommand(true);
		}
	}	
	
	@GlobalCommand
	public void openVersioningManagerPopUp() {
		if ( versioningPopUp == null ) {
			dispatchCurrentFormValidatedCommand(true);
			versioningPopUp = openPopUp(Resources.Component.VERSIONING_POPUP.getLocation(), true);
		}
	}

	@GlobalCommand
	public void closeVersioningManagerPopUp() {
		if ( versioningPopUp != null ) {
			closePopUp(versioningPopUp);
			versioningPopUp = null;
			dispatchCurrentFormValidatedCommand(true);
		}
	}
	
	@Command
	public void backToSurveysList() {
		if ( changed ) {
			MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performBackToSurveysList();
				}
			}, "survey.edit.leave_page_with_unsaved_changes");
			params.setTitleKey("global.unsaved_changes");;
			params.setOkLabelKey("global.continue_and_loose_changes");
			params.setCancelLabelKey("global.stay_on_this_page");
			MessageUtil.showConfirm(params);
		} else {
			performBackToSurveysList();
		}
	}
	
	protected void performBackToSurveysList() {
		PageUtil.clearConfirmClose();
		resetSessionStatus();
		showMainPage();
	}

	protected void showMainPage() {
		Executions.sendRedirect(Resources.Page.DESIGNER.getLocation());
	}

	protected void resetSessionStatus() {
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.reset();
	}
	
	@Command
	@NotifyChange({"currentLanguageCode"})
	public void languageCodeSelected(@BindingParam("code") final String selectedLanguageCode) {
		final SessionStatus sessionStatus = getSessionStatus();
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				sessionStatus.setCurrentLanguageCode(selectedLanguageCode);
				BindUtils.postGlobalCommand(null, null, SurveyLanguageVM.CURRENT_LANGUAGE_CHANGED_COMMAND, null);
				currentLanguageCode = sessionStatus.getCurrentLanguageCode();
			}
		});
	}
	
	@Command
	public void save(@ContextParam(ContextType.BINDER) Binder binder) throws SurveyStoreException {
		dispatchValidateAllCommand();
		if ( checkCanSave() ) {
			backgroundSurveySave();
		}
	}
	
	@GlobalCommand
	public void backgroundSurveySave() throws SurveyStoreException {
		//survey.refreshSurveyDependencies();
		surveyManager.saveSurveyWork(survey);
		BindUtils.postNotifyChange(null, null, survey, "id");
		BindUtils.postNotifyChange(null, null, survey, "published");
		changed = false;
		notifyChange("surveyStored","surveyId","surveyPublished","surveyChanged");
		dispatchSurveySavedCommand();
	}
	
	private void dispatchSurveySavedCommand() {
		BindUtils.postGlobalCommand(null, null, SURVEY_SAVED_GLOBAL_COMMAND, null);
	}

	protected boolean checkCanSave() {
		if ( checkCanLeaveForm() ) {
			if ( ! checkSurveyNameUniqueness() ) {
				return false;
			} else if ( ! checkSurveyUriUniqueness() ) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	private boolean checkSurveyNameUniqueness() {
		SurveySummary existingSurveySummary = surveyManager.loadSummaryByName(survey.getName());
		if ( existingSurveySummary != null && ! isCurrentEditedSurvey(existingSurveySummary) ) {
			String messageKey = LabelKeys.SURVEY_SAVE_ERROR_DUPLICATE_NAME;
			MessageUtil.showWarning(messageKey);
			return false;
		} else {
			return true;
		}
	}

	private boolean checkSurveyUriUniqueness() {
		SurveySummary existingSurveySummary = surveyManager.loadSummaryByUri(survey.getUri());
		if ( existingSurveySummary != null && ! isCurrentEditedSurvey(existingSurveySummary) ) {
			String messageKey = LabelKeys.SURVEY_SAVE_ERROR_DUPLICATE_URI;
			MessageUtil.showWarning(messageKey);
			return false;
		} else {
			return true;
		}
	}
	
	@Command
	public void validate() {
		if ( checkValidity(false) ) {
			MessageUtil.showInfo("survey.successfully_validated");
		}
	}
	
	private boolean checkValidity(boolean showConfirm) {
		SurveyValidationResults results = surveyValidator.validate(survey);
		if ( results.hasErrors() || results.hasWarnings() ) {
			validationResultsPopUp = SurveyValidationResultsVM.showPopUp(results, showConfirm);
			return false;
		} else {
			return true;
		}
	}
	
	@GlobalCommand
	public void confirmValidationResultsPopUp() {
		if ( validationResultsPopUp != null ) {
			closePopUp(validationResultsPopUp);
			validationResultsPopUp = null;
			if ( showingPreview ) {
				openPreviewPopUp();
			}
		}
	}
	
	@GlobalCommand
	public void closeValidationResultsPopUp() {
		closePopUp(validationResultsPopUp);
		validationResultsPopUp = null;
	}
	
	@Command
	public void exportSchemaSummary() {
		SchemaSummaryCSVExportJob job = new SchemaSummaryCSVExportJob();
		job.setJobManager(jobManager);
		job.setSurvey(survey);
		jobManager.start(job, survey.getId().toString());
		
		String statusPopUpTitle = Labels.getLabel("survey.schema.export_summary.process_status_popup.message", new String[] { survey.getName() });
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(statusPopUpTitle, job, true);
	}
	
	@GlobalCommand
	public void jobCompleted(@BindingParam("job") Job job) {
		closeJobStatusPopUp();
		if ( job instanceof SchemaSummaryCSVExportJob ) {
			File file = ((SchemaSummaryCSVExportJob) job).getOutputFile();
			String surveyName = survey.getName();
			String dateStr = Dates.formatLocalDateTime(new Date());
			String fileName = String.format(SCHEMA_SUMMARY_FILE_NAME_PATTERN, surveyName, dateStr, "csv");
			String contentType = URLConnection.guessContentTypeFromName(fileName);
			try {
				FileInputStream is = new FileInputStream(file);
				Filedownload.save(is, contentType, fileName);
			} catch (FileNotFoundException e) {
				log.error(e);
				MessageUtil.showError("survey.schema.export_summary.error", new String[]{e.getMessage()});
			}
		}
	}

	private void closeJobStatusPopUp() {
		closePopUp(jobStatusPopUp);
		jobStatusPopUp = null;
	}

	private void openPreviewPopUp() {
		if ( isSingleRootEntityDefined() && survey.getVersions().size() <= 1 ) {
			ModelVersion version = survey.getVersions().isEmpty() ? null: survey.getVersions().get(0);
			openPreviewPopUp(version, survey.getSchema().getRootEntityDefinitions().get(0));
		} else {
			openPreviewPreferencesPopUp();
		}
	}

	@GlobalCommand
	public void openPreviewPopUp(@BindingParam("formVersion") ModelVersion formVersion, @BindingParam("rootEntity") EntityDefinition rootEntity) {
		if ( validateShowPreview(rootEntity, formVersion) ) {
			if ( rootEntity == null ) {
				rootEntity = survey.getSchema().getRootEntityDefinitions().get(0);
			}
			survey.refreshSurveyDependencies();
			
			if (survey.getTarget() == SurveyTarget.COLLECT_EARTH) {
				openPopUp(Resources.Component.COLLECT_EARTH_PREVIEW_POPUP.getLocation(), true);
			} else {
				Map<String, String> params = createBasicModuleParameters();
				params.put("preview", "true");
				params.put("surveyId", Integer.toString(survey.getId()));
				params.put("work", "true");
				params.put("rootEntityId", Integer.toString(rootEntity.getId()));
				if ( formVersion != null ) {
					params.put("versionId", Integer.toString(formVersion.getId()));
				}
				openPopUp(Resources.Component.PREVIEW_POP_UP.getLocation(), true, params);

				closePreviewPreferencesPopUp();
			}
		}
	}
	
	protected boolean validateShowPreview(EntityDefinition rootEntityDefn, ModelVersion version) {
		if ( rootEntityDefn == null && ! isSingleRootEntityDefined() ) {
			MessageUtil.showWarning(LabelKeys.PREVIEW_ROOT_ENTITY_NOT_SPECIFIED);
			return false;
		} else if (version == null && survey.getVersions() != null && ! survey.getVersions().isEmpty() ) {
			MessageUtil.showWarning(LabelKeys.PREVIEW_ERROR_VERSION_NOT_SPECIFIED);
			return false;
		} else {
			return true;
		}
	}

	@Command
	public void showPreview() {
		if (survey.getId() == null || changed)  {
			MessageUtil.showWarning(LabelKeys.PREVIEW_ERROR_SAVE_SURVEY_FIRST);
		} else {
			showingPreview = true;
			if ( checkValidity(true) ) {
				openPreviewPopUp();
			}
		}
	}

	protected void openPreviewPreferencesPopUp() {
		previewPreferencesPopUp = openPopUp(Resources.Component.PREVIEW_PREFERENCES_POP_UP.getLocation(), true);
	}
	
	@GlobalCommand
	public void closePreviewPreferencesPopUp() {
		closePopUp(previewPreferencesPopUp);
		previewPreferencesPopUp = null;
		showingPreview = false;
	}
	
	@GlobalCommand
	public void surveyChanged() {
		changed = true;
		notifyChange("surveyChanged");
	}

	@GlobalCommand
	@NotifyChange({"availableLanguages"})
	public void surveyLanguagesChanged() {
		closeSurveyLanguageSelectPopUp();
	}
	
	@GlobalCommand
	public void closeSurveyLanguageSelectPopUp() {
		closePopUp(selectLanguagePopUp);
		selectLanguagePopUp = null;
	}
	
	public List<String> getAvailableLanguages() {
		CollectSurvey survey = getSurvey();
		if ( survey == null ) {
			//TODO session expired?
			return null;
		} else {
			List<String> languages = survey.getLanguages();
			return new BindingListModelList<String>(languages, false);
		}
	}

	@Override
	public boolean isSurveyChanged() {
		return changed;
	}
	
	@DependsOn({"surveyId","surveyPublished"})
	public String getSamplingDesignImportModuleUrl() {
		Map<String, String> queryParams = createBasicModuleParameters();
		queryParams.put("sampling_design_import", "true");
		String url = ComponentUtil.createUrl(Resources.Page.COLLECT_SWF.getLocation(), queryParams);
		return url;
	}

	@DependsOn({"surveyId","surveyPublished"})
	public String getSpeciesImportModuleUrl() {
		Map<String, String> queryParams = createBasicModuleParameters();
		queryParams.put("species_import", "true");
		String url = ComponentUtil.createUrl(Resources.Page.COLLECT_SWF.getLocation(), queryParams);
		return url;
	}

}
