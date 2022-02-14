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
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.PageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.OutputFormat;
import org.openforis.collect.designer.viewmodel.SurveyValidationResultsVM.ConfirmEvent;
import org.openforis.collect.io.data.CSVDataExportJob;
import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.collect.io.metadata.SchemaSummaryCSVExportJob;
import org.openforis.collect.io.metadata.collectearth.CollectEarthGridTemplateGenerator;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.manager.validation.CollectMobileSurveyValidator;
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
import org.openforis.collect.utils.MediaTypes;
import org.openforis.collect.web.ws.AppWS;
import org.openforis.collect.web.ws.AppWS.MessageType;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
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

	private static final Log log = Log.lookup(SurveyEditVM.class);

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
	private Window schemaLabelsImportPopUp;

	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private SurveyValidator surveyValidator;
	@WireVariable
	private CollectEarthSurveyValidator collectEarthSurveyValidator;
	@WireVariable
	private CollectMobileSurveyValidator collectMobileSurveyValidator;
	@WireVariable
	private AppWS appWS;
	
	private boolean changed;
	private Window jobStatusPopUp;

	private Step previewStep;

	public static void redirectToSurveyEditPage(int surveyId) {
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation() + "?id=" + surveyId);
	}
	
	public static void dispatchSurveySaveCommand() {
		BindUtils.postGlobalCommand(null, null, SurveyEditVM.BACKGROUD_SAVE_GLOBAL_COMMAND, null);
	}
	
	@Init(superclass=false)
	public void init(@QueryParam("id") Integer surveyId) {
		super.init();
		survey = surveyManager.loadSurvey(surveyId);
		
		if (survey == null || ! survey.isTemporary()) {
			backToSurveysList();
		} else {
			SessionStatus sessionStatus = getSessionStatus();
			Integer publishedSurveyId = null;
			if (survey.isPublished()) {
				if (survey.isTemporary()) {
					publishedSurveyId = survey.getPublishedId();
				} else {
					publishedSurveyId = survey.getId();
				}
			}
			sessionStatus.setPublishedSurveyId(publishedSurveyId);
			sessionStatus.setSurvey(survey);
			
			changed = false;
			currentLanguageCode = survey.getDefaultLanguage();
			if ( currentLanguageCode == null ) {
				openLanguageManagerPopUp();
			} else {
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
	public void openSchemaLabelsImportExportPopUp() {
		if ( checkCanLeaveForm() ) {
			schemaLabelsImportPopUp = openPopUp(Resources.Component.SCHEMA_LABELS_IMPORT_POP_UP.getLocation(), true);
		}
	}
	
	@GlobalCommand
	public void closeSchemaLabelsImportPopUp() {
		closePopUp(schemaLabelsImportPopUp);
		schemaLabelsImportPopUp = null;
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
			Map<String, Object> args = new HashMap<>();
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
		Map<String, Object> args = new HashMap<>();
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
			MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(() -> performBackToSurveysList(),
					"survey.edit.leave_page_with_unsaved_changes");
			params.setTitleKey("global.unsaved_changes");
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
			// session could be expired
			return null;
		} else {
			List<String> languages = survey.getLanguages();
			return new BindingListModelList<>(languages, false);
		}
	}

	@Command
	@NotifyChange({"currentLanguageCode"})
	public void languageCodeSelected(@BindingParam("code") final String selectedLanguageCode) {
		final SessionStatus sessionStatus = getSessionStatus();
		checkCanLeaveForm(confirmed -> {
			sessionStatus.setCurrentLanguageCode(selectedLanguageCode);
			BindUtils.postGlobalCommand(null, null, SurveyLanguageVM.CURRENT_LANGUAGE_CHANGED_COMMAND, null);
			currentLanguageCode = sessionStatus.getCurrentLanguageCode();
		});
	}

	/**
	 * Returns true if there wasn't any error and the survey has been saved immediately without showing any confirm PopUp
	 */
	@Command
	public boolean save(@ContextParam(ContextType.BINDER) Binder binder, 
			final Runnable runAfterSave) {
		dispatchValidateAllCommand();
		if ( checkCanSave() ) {
			return checkValidity(true, () -> {
				try {
					backgroundSurveySave();
					if (runAfterSave != null) {
						runAfterSave.run();
					}
				} catch (SurveyStoreException e) {
					throw new RuntimeException(e);
				}
			}, Labels.getLabel("survey.save.confirm_save_with_errors"), true);
		} else {
			return false;
		}
	}
	
	@GlobalCommand
	public void backgroundSurveySave() throws SurveyStoreException {
		surveyManager.save(survey);
		BindUtils.postNotifyChange(null, null, survey, "id");
		BindUtils.postNotifyChange(null, null, survey, "published");
		changed = false;
		notifyChange("surveyStored","surveyId","surveyPublished","surveyChanged");
		dispatchSurveySavedCommand();
		appWS.sendMessage(MessageType.SURVEYS_UPDATED);
	}
	
	private void dispatchSurveySavedCommand() {
		BindUtils.postGlobalCommand(null, null, SURVEY_SAVED_GLOBAL_COMMAND, null);
	}

	protected boolean checkCanSave() {
		if ( checkCanLeaveForm() ) {
			return checkSurveyNameUniqueness() && checkSurveyUriUniqueness();
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
		checkValidity(OutputFormat.DESKTOP);
	}
	
	@Command
	public void validateCollectMobile() {
		checkValidity(OutputFormat.MOBILE);
	}
	
	private boolean checkValidity(OutputFormat outputFormat) {
		return checkValidity(outputFormat, false, () -> MessageUtil.showInfo("survey.successfully_validated"), null, false);
	}

	private boolean checkValidity(boolean showConfirm, final Runnable runIfValid, 
			String confirmButtonLabel, boolean ignoreWarnings) {
		return checkValidity(OutputFormat.DESKTOP, showConfirm, runIfValid, confirmButtonLabel, ignoreWarnings);
	}
	
	/**
	 * Returns true if the validation didn't give any errors, false if a confirm PopUp will be shown
	 */
	private boolean checkValidity(OutputFormat outuputFormat, boolean showConfirm, final Runnable runIfValid, 
			String confirmButtonLabel, boolean ignoreWarnings) {
		SurveyValidator validator = getSurveyValidator(survey, outuputFormat);
		ValidationParameters validationParameters = new ValidationParameters();
		validationParameters.setWarningsIgnored(ignoreWarnings);
		SurveyValidationResults results = validator.validate(survey, validationParameters);
		if ( results.hasErrors() || results.hasWarnings() ) {
			final Window validationResultsPopUp = SurveyValidationResultsVM.showPopUp(results, showConfirm, 
					confirmButtonLabel);
			validationResultsPopUp.addEventListener(SurveyValidationResultsVM.CONFIRM_EVENT_NAME, new EventListener<ConfirmEvent>() {
				public void onEvent(ConfirmEvent event) throws Exception {
					runIfValid.run();
					closePopUp(validationResultsPopUp);
				}
			});
			return false;
		} else {
			runIfValid.run();
			return true;
		}
	}
	
	@Command
	public void exportSchemaSummary() {
		SchemaSummaryCSVExportJob job = new SchemaSummaryCSVExportJob();
		job.setJobManager(jobManager);
		job.setSurvey(survey);
		jobManager.start(job, survey.getId().toString());
		
		String statusPopUpTitle = Labels.getLabel("survey.schema.export_summary.process_status_popup.message", new String[] { survey.getName() });
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(statusPopUpTitle, job, true, new JobEndHandler<SchemaSummaryCSVExportJob>() {
			public void onJobEnd(SchemaSummaryCSVExportJob job) {
				closePopUp(jobStatusPopUp);
				jobStatusPopUp = null;
				File file = job.getOutputFile();
				String surveyName = survey.getName();
				String dateStr = Dates.formatLocalDateTime(new Date());
				String fileName = String.format(SCHEMA_SUMMARY_FILE_NAME_PATTERN, surveyName, dateStr, Files.EXCEL_FILE_EXTENSION);
				String contentType = URLConnection.guessContentTypeFromName(fileName);
				try {
					FileInputStream is = new FileInputStream(file);
					Filedownload.save(is, contentType, fileName);
				} catch (FileNotFoundException e) {
					log.error(e);
					MessageUtil.showError("survey.schema.export_summary.error", e.getMessage());
				}
			}
		});
	}
	
	@Command
	public void exportCsvDataImportTemplate() throws IOException {
		CSVDataExportJob job = jobManager.createJob(CSVDataExportJob.class);
		job.setOutputFile(File.createTempFile("data-import-template", ".zip"));
		
		CSVDataExportParameters parameters = new CSVDataExportParameters();
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		RecordFilter recordFilter = new RecordFilter(survey, rootEntityDef.getId());
		parameters.setRecordFilter(recordFilter);
		parameters.setAlwaysGenerateZipFile(true);
		parameters.setIncludeEnumeratedEntities(false);
		job.setParameters(parameters);

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
		EntityDefinition rootEntityDef = survey.getSchema().getFirstRootEntityDefinition();
		recordFilter.setRootEntityId(rootEntityDef.getId());

		CSVDataExportParameters parameters = new CSVDataExportParameters();
		parameters.setRecordFilter(recordFilter);
		parameters.setEntityId(rootEntityDef.getId());
		parameters.setAlwaysGenerateZipFile(false);
		parameters.setIncludeEnumeratedEntities(true);
		job.setParameters(parameters);

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
		Filedownload.save(new FileInputStream(templateFile), MediaTypes.CSV_CONTENT_TYPE, fileName);
	}
	
	private void openPreviewPopUp() {
		if ( isSingleRootEntityDefined() && survey.getVersions().size() <= 1 ) {
			ModelVersion version = survey.getVersions().isEmpty() ? null: survey.getVersions().get(0);
			openPreviewPopUp(version, survey.getSchema().getFirstRootEntityDefinition());
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
				rootEntity = survey.getSchema().getFirstRootEntityDefinition();
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
		if (! checkCanLeaveForm() ) {
			return;
		}
		previewStep = recordStep;

		if (survey.getId() == null || changed)  {
			save(null, this::openPreviewPopUp);
		} else {
			checkValidity(true, this::openPreviewPopUp, Labels.getLabel("survey.preview.show_preview"), true);
		}
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
	
	private SurveyValidator getSurveyValidator(CollectSurvey survey, OutputFormat outputFormat) {
		if (survey.getTarget() == SurveyTarget.COLLECT_EARTH) {
			return collectEarthSurveyValidator;
		}
		if (outputFormat == OutputFormat.MOBILE) {
			return collectMobileSurveyValidator;
		}
		return surveyValidator;
	}

}
