/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.viewmodel.CodeListsVM.EDITING_ATTRIBUTE_PARAM;
import static org.openforis.collect.designer.viewmodel.CodeListsVM.SELECTED_CODE_LIST_PARAM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.openforis.collect.designer.util.SuccessHandler;
import org.openforis.collect.designer.viewmodel.SurveyValidationResultsVM.ConfirmEvent;
import org.openforis.collect.io.data.CSVDataExportJob;
import org.openforis.collect.io.data.csv.CSVExportConfiguration;
import org.openforis.collect.io.metadata.SchemaSummaryCSVExportJob;
import org.openforis.collect.io.metadata.collectearth.CollectEarthGridTemplateGenerator;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.manager.validation.SurveyValidator.ValidationParameters;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.Files;
import org.openforis.concurrency.Job;
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
import org.zkoss.zk.ui.event.EventListener;
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

	public static final String SHOW_PREVIEW_POP_UP_GLOBAL_COMMAND = "showPreview";
	public static final String BACKGROUD_SAVE_GLOBAL_COMMAND = "backgroundSurveySave";
	private static final String CODE_LISTS_POP_UP_CLOSED_COMMAND = "codeListsPopUpClosed";
	
	private static final String SCHEMA_SUMMARY_FILE_NAME_PATTERN = "%s_schema_summary_%s.%s";
	private static final String DATA_IMPORT_TEMPLATE_FILE_NAME_PATTERN = "%s_data_import_template_%s.%s";
	
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
	@WireVariable
	private CollectEarthSurveyValidator collectEarthSurveyValidator;
	
	private boolean changed;
	private Window jobStatusPopUp;

	private Step previewStep;

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

	@Command
	public void openSchemaAttributesImportPopUp() {
		if ( checkCanLeaveForm() ) {
			openPopUp(Resources.Component.SCHEMA_ATTRIBUTES_IMPORT_POP_UP.getLocation(), true);
		}
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
			@BindingParam(EDITING_ATTRIBUTE_PARAM) Boolean editingAttribute, 
			@BindingParam(SELECTED_CODE_LIST_PARAM) CodeList selectedCodeList) {
		if ( codeListsPopUp == null ) { 
			dispatchCurrentFormValidatedCommand(true);
			Map<String, Object> args = new HashMap<String, Object>();
			args.put(EDITING_ATTRIBUTE_PARAM, editingAttribute);
			CodeList selectedCodeListInPopUp = selectedCodeList == survey.getSamplingDesignCodeList() ? null: selectedCodeList;
			args.put(SELECTED_CODE_LIST_PARAM, selectedCodeListInPopUp);
			codeListsPopUp = openPopUp(Resources.Component.CODE_LISTS_POPUP.getLocation(), true, args);
		}
	}

	@GlobalCommand
	public void closeCodeListsManagerPopUp(@ContextParam(ContextType.BINDER) Binder binder,
			@BindingParam(EDITING_ATTRIBUTE_PARAM) final Boolean editingAttribute,
			@BindingParam(SELECTED_CODE_LIST_PARAM) final CodeList selectedCodeList) {
		if ( codeListsPopUp != null ) {
			closePopUp(codeListsPopUp);
			codeListsPopUp = null;
			dispatchCurrentFormValidatedCommand(true);
			dispatchCodeListsPopUpClosedCommand(editingAttribute, selectedCodeList);
		}
	}

	public void dispatchCodeListsPopUpClosedCommand(Boolean editingAttribute, CodeList selectedCodeList) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(EDITING_ATTRIBUTE_PARAM, editingAttribute);
		args.put(SELECTED_CODE_LIST_PARAM, selectedCodeList);
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
			checkValidity(true, new SuccessHandler() {
				public void onSuccess() {
					try {
						backgroundSurveySave();
					} catch (SurveyStoreException e) {
						throw new RuntimeException(e);
					}
				}
			}, Labels.getLabel("survey.save.confirm_save_with_errors"), false);
		}
	}
	
	@GlobalCommand
	public void backgroundSurveySave() throws SurveyStoreException {
		//survey.refreshSurveyDependencies();
		surveyManager.save(survey);
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
		checkValidity(false, new SuccessHandler() {
			public void onSuccess() {
				MessageUtil.showInfo("survey.successfully_validated");
			}
		}, null, true);
	}
	
	private void checkValidity(boolean showConfirm, final SuccessHandler successHandler, 
			String confirmButtonLabel, boolean showWarnings) {
		SurveyValidator surveyValidator = getSurveyValidator(survey);
		ValidationParameters validationParameters = new ValidationParameters();
		validationParameters.setWarnOnEmptyCodeLists(showWarnings);
		validationParameters.setWarnOnUnusedCodeLists(showWarnings);
		SurveyValidationResults results = surveyValidator.validate(survey, validationParameters);
		if ( results.hasErrors() || results.hasWarnings() ) {
			final Window validationResultsPopUp = SurveyValidationResultsVM.showPopUp(results, showConfirm, 
					confirmButtonLabel);
			validationResultsPopUp.addEventListener(SurveyValidationResultsVM.CONFIRM_EVENT_NAME, new EventListener<ConfirmEvent>() {
				public void onEvent(ConfirmEvent event) throws Exception {
					successHandler.onSuccess();
					closePopUp(validationResultsPopUp);
				}
			});
		} else {
			successHandler.onSuccess();;
		}
	}
	
	@Command
	public void exportSchemaSummary() {
		SchemaSummaryCSVExportJob job = new SchemaSummaryCSVExportJob();
		job.setJobManager(jobManager);
		job.setSurvey(survey);
		job.setLabelLanguage(currentLanguageCode);
		jobManager.start(job, survey.getId().toString());
		
		String statusPopUpTitle = Labels.getLabel("survey.schema.export_summary.process_status_popup.message", new String[] { survey.getName() });
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(statusPopUpTitle, job, true);
	}
	
	@Command
	public void exportCsvDataImportTemplate() throws IOException {
		CSVDataExportJob job = jobManager.createJob(CSVDataExportJob.class);
		job.setOutputFile(File.createTempFile("data-import-template", ".zip"));
		RecordFilter recordFilter = new RecordFilter(survey);
		EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinitions().get(0);
		recordFilter.setRootEntityId(rootEntityDef.getId());
		job.setRecordFilter(recordFilter);
		job.setAlwaysGenerateZipFile(true);
		CSVExportConfiguration configuration = new CSVExportConfiguration();
		configuration.setIncludeEnumeratedEntities(false);
		job.setConfiguration(configuration);
		jobManager.start(job, false);
		if (job.isCompleted()) {
			File outputFile = job.getOutputFile();
			String dateStr = Dates.formatLocalDateTime(new Date());
			String fileName = String.format(DATA_IMPORT_TEMPLATE_FILE_NAME_PATTERN, survey.getName(), dateStr, "zip");
			String contentType = URLConnection.guessContentTypeFromName(fileName);
			FileInputStream is = new FileInputStream(outputFile);
			Filedownload.save(is, contentType, fileName);
		} else {
			throw new RuntimeException("Error generating the CSV data export template: " + job.getErrorMessage(), 
					job.getLastException());
		}
	}
	
	@Command
	public void exportCeCsvDataImportTemplate() throws IOException {
		CSVDataExportJob job = jobManager.createJob(CSVDataExportJob.class);
		job.setOutputFile(File.createTempFile("ce-data-import-template-" + survey.getName(), ".csv"));
		RecordFilter recordFilter = new RecordFilter(survey);
		EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinitions().get(0);
		recordFilter.setRootEntityId(rootEntityDef.getId());
		job.setRecordFilter(recordFilter);
		job.setEntityId(rootEntityDef.getId());
		job.setAlwaysGenerateZipFile(false);
		CSVExportConfiguration configuration = new CSVExportConfiguration();
		configuration.setIncludeEnumeratedEntities(true);
		job.setConfiguration(configuration);
		jobManager.start(job, false);
		if (job.isCompleted()) {
			File outputFile = job.getOutputFile();
			String dateStr = Dates.formatLocalDateTime(new Date());
			String fileName = String.format(DATA_IMPORT_TEMPLATE_FILE_NAME_PATTERN, survey.getName(), dateStr, "csv");
			String contentType = URLConnection.guessContentTypeFromName(fileName);
			FileInputStream is = new FileInputStream(outputFile);
			Filedownload.save(is, contentType, fileName);
		} else {
			throw new RuntimeException("Error generating the CSV data export template: " + job.getErrorMessage(), 
					job.getLastException());
		}
	}
	
	@Command
	public void exportSurvey() throws IOException {
		SurveyExportParametersVM.openPopUp(SurveySummary.createFromSurvey(survey));
	}
	
	@Command
	public void exportCEGridTemplate() throws IOException {
		File templateFile = new CollectEarthGridTemplateGenerator().generateTemplateCSVFile(survey);
		String fileName = String.format("%s_grid_template_%s.csv", survey.getName(), Dates.formatDateTime(new Date()));
		Filedownload.save(new FileInputStream(templateFile),  Files.CSV_CONTENT_TYPE, fileName);
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
	public void openPreviewPopUp(
			@BindingParam("formVersion") ModelVersion formVersion, 
			@BindingParam("rootEntity") EntityDefinition rootEntity) {
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
				params.put("recordStep", previewStep.name());
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
	public void showDataEntryPreview() throws SurveyStoreException {
		showPreview(Step.ENTRY);
	}
	
	@Command
	public void showDataCleansingPreview() throws SurveyStoreException {
		showPreview(Step.CLEANSING);
	}

	public void showPreview(Step recordStep) throws SurveyStoreException {
		if (survey.getId() == null || changed)  {
			save(null);
		}
		previewStep = recordStep;
		checkValidity(true, new SuccessHandler() {
			public void onSuccess() {
				openPreviewPopUp();
			}
		}, Labels.getLabel("survey.preview.show_preview"), false);
	}

	protected void openPreviewPreferencesPopUp() {
		previewPreferencesPopUp = openPopUp(Resources.Component.PREVIEW_PREFERENCES_POP_UP.getLocation(), true);
	}
	
	@GlobalCommand
	public void closePreviewPreferencesPopUp() {
		closePopUp(previewPreferencesPopUp);
		previewPreferencesPopUp = null;
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

	public String getSurveyEditTitle() {
		String surveyName = survey == null ? null: survey.getName();
		return Labels.getLabel("designer_title_editing_survey", new Object[] {surveyName});
	}
	
	private SurveyValidator getSurveyValidator(CollectSurvey survey) {
		return survey.getTarget() == SurveyTarget.COLLECT_EARTH ? collectEarthSurveyValidator : surveyValidator;
	}

}
