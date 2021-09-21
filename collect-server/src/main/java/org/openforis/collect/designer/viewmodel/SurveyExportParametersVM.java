package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType.PUBLISHED;
import static org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType.TEMPORARY;
import static org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.OutputFormat.DESKTOP;
import static org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.OutputFormat.EARTH;
import static org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.OutputFormat.RDB;
import static org.openforis.collect.metamodel.SurveyTarget.COLLECT_EARTH;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.util.SuccessHandler;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType;
import org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.OutputFormat;
import org.openforis.collect.designer.viewmodel.SurveyValidationResultsVM.ConfirmEvent;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.DataBackupError;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreator;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreatorImpl;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.manager.validation.CollectMobileSurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResults;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.relational.print.RDBPrintJob;
import org.openforis.collect.relational.print.RDBPrintJob.RdbDialect;
import org.openforis.collect.utils.Dates;
import org.openforis.concurrency.Job;
import org.springframework.http.MediaType;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.logging.Log;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyExportParametersVM extends BaseVM {
	
	private static Log LOG = Log.lookup(SurveyExportParametersVM.class);
	
	private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * Pattern for survey export file name (SURVEYNAME_DATE.OUTPUTFORMAT)
	 */
	private static final String SURVEY_EXPORT_FILE_NAME_PATTERN = "%s_%s.%s";
	private static final String SURVEY_EXPORT_MOBILE_FILE_NAME_PATTERN = "%s_%s_%s.%s";
	
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
	@WireVariable
	private CollectMobileSurveyValidator collectMobileSurveyValidator;
	
	private SurveySummary surveySummary;
	private SurveyExportParametersFormObject formObject;
	private Form tempForm;
	
	private Window jobStatusPopUp;

	public static void openPopUp(SurveySummary surveySummary) throws IOException {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("survey", surveySummary);
		openPopUp(Resources.Component.SURVEY_EXPORT_PARAMETERS_POPUP.getLocation(), true, args);
	}
	
	@Init
	public void init(@ExecutionArgParam("survey") SurveySummary survey) {
		this.surveySummary = survey;
		this.formObject = new SurveyExportParametersFormObject();
		String outputFormat = (survey.getTarget() == COLLECT_EARTH ? EARTH : DESKTOP).name();
		this.formObject.setOutputFormat(outputFormat);
		this.formObject.setType((survey.isNotLinkedToPublishedSurvey() ? TEMPORARY: PUBLISHED).name());
		this.formObject.setRdbDialect(RdbDialect.STANDARD.name());
		this.formObject.setRdbDateTimeFormat(DEFAULT_DATE_TIME_FORMAT);
		this.formObject.setRdbTargetSchemaName(survey.getName());
		this.formObject.setLanguageCode(survey.getDefaultLanguage());
		this.tempForm = new SimpleForm();
	}
	
	@Command
	public void typeChanged() {
		checkEnabledFields();
	}
	
	@Command
	public void outputFormatChanged() {
		checkEnabledFields();
	}
	
	@Command
	public void includeDataChanged() {
		checkEnabledFields();
	}
	
	@Command
	public void export() {
		String uri = surveySummary.getUri();
		final CollectSurvey loadedSurvey;
		if ( surveySummary.isTemporary() && SurveyType.valueOf(formObject.getType()) == TEMPORARY ) {
			loadedSurvey = surveyManager.loadSurvey(surveySummary.getId());
		} else {
			loadedSurvey = surveyManager.getByUri(uri);
		}
		switch(formObject.getOutputFormatEnum()) {
		case EARTH:
			validateSurvey(loadedSurvey, collectEarthSurveyValidator, new SuccessHandler() {
				public void onSuccess() {
					exportCollectEarthSurvey(loadedSurvey, formObject);
				}
			}, true);
			return;
		case RDB:
			startRDBSurveyExportJob(loadedSurvey, formObject);
			break;
		case MOBILE:
			validateSurvey(loadedSurvey, collectMobileSurveyValidator, new SuccessHandler() {
				public void onSuccess() {
					startCollectSurveyExportJob(loadedSurvey, formObject);
				}
			}, true);
			break;
		default:
			startCollectSurveyExportJob(loadedSurvey, formObject);
			break;
		}
	}
	
	private void downloadFile(File file, String extension, String contentType, CollectSurvey survey, String defaultLanguageCode) {
		String surveyName = survey.getName();
		String dateStr = Dates.formatLocalDateTime(survey.getModifiedDate());
		String fileName;
		if (org.openforis.collect.io.SurveyBackupJob.OutputFormat.MOBILE.getOutputFileExtension().equals(extension)) {
			fileName = String.format(SURVEY_EXPORT_MOBILE_FILE_NAME_PATTERN, surveyName, defaultLanguageCode, dateStr, extension);
		} else {
			fileName = String.format(SURVEY_EXPORT_FILE_NAME_PATTERN, surveyName, dateStr, extension);
		}
		try {
			Filedownload.save(new FileInputStream(file), contentType, fileName);
		} catch (FileNotFoundException e) {
			LOG.error(e);
			MessageUtil.showError("survey.export_survey.error", e.getMessage());
		}
	}
	
	private void startRDBSurveyExportJob(final CollectSurvey survey,
			final SurveyExportParametersFormObject parameters) {
		RDBPrintJob job = new RDBPrintJob();
		job.setSurvey(survey);
		job.setTargetSchemaName(survey.getName());
		job.setRecordManager(recordManager);
		RecordFilter recordFilter = new RecordFilter(survey);
		job.setRecordFilter(recordFilter);
		job.setIncludeData(parameters.isIncludeData());
		job.setDialect(parameters.getRdbDialectEnum());
		job.setDateTimeFormat(parameters.getRdbDateTimeFormat());
		job.setTargetSchemaName(parameters.getRdbTargetSchemaName());
		jobManager.start(job, String.valueOf(survey.getId()));
		openJobStatusPopUp(survey.getName(), job, new ExportJobEndHandler<RDBPrintJob>() {
			@Override
			protected void onJobCompleted() {
				File file = job.getOutputFile();
				CollectSurvey survey = job.getSurvey();
				String extension = "sql";
				downloadFile(file, extension, MediaType.TEXT_PLAIN_VALUE, survey, survey.getDefaultLanguage());
				super.onJobCompleted();
			}
		});
	}
	
	private  <J extends Job> void openJobStatusPopUp(String surveyName, J job, JobEndHandler<J> jobEndHandler) {
		String title = Labels.getLabel("survey.export_survey.process_status_popup.message", new String[] { surveyName });
		jobStatusPopUp = JobStatusPopUpVM.openPopUp(title, job, true, jobEndHandler);
	}

	private void exportCollectEarthSurvey(final CollectSurvey survey,
			final SurveyExportParametersFormObject parameters) {
		try {
			CollectEarthProjectFileCreatorImpl creatorImpl = (CollectEarthProjectFileCreatorImpl) COLLECT_EARTH_PROJECT_FILE_CREATOR;
			creatorImpl.setCodeListManager(codeListManager);
			creatorImpl.setSurveyManager(surveyManager);
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
			LOG.error(e);
			MessageUtil.showError("survey.export.error_generating_collect_earth_project_file", e.getMessage());
		}
	}

	protected void startCollectSurveyExportJob(CollectSurvey survey,
			SurveyExportParametersFormObject parameters) {
		SurveyBackupJob job = jobManager.createJob(SurveyBackupJob.class);
		job.setSurvey(survey);
		job.setIncludeData(parameters.isIncludeData());
		job.setIncludeRecordFiles(parameters.isIncludeUploadedFiles());
		job.setOutputFormat(org.openforis.collect.io.SurveyBackupJob.OutputFormat.valueOf(parameters.getOutputFormat()));
		job.setOutputSurveyDefaultLanguage(ObjectUtils.defaultIfNull(parameters.getLanguageCode(), survey.getDefaultLanguage()));
		jobManager.start(job, String.valueOf(survey.getId()));
		openJobStatusPopUp(survey.getName(), job, new ExportJobEndHandler<SurveyBackupJob>() {
			@Override
			protected void onJobCompleted() {
				File file = job.getOutputFile();
				downloadFile(file, job.getOutputFormat().getOutputFileExtension(), MediaType.APPLICATION_OCTET_STREAM_VALUE, 
						job.getSurvey(), job.getOutputSurveyDefaultLanguage());
				final List<DataBackupError> dataBackupErrors = job.getDataBackupErrors();
				if (! dataBackupErrors.isEmpty()) {
					DataExportErrorsPopUpVM.showPopUp(dataBackupErrors);
				}
				super.onJobCompleted();
			}
		});
	}
	
	private class ExportJobEndHandler<J extends Job> implements JobEndHandler<J> {
		public void onJobEnd(J job) {
			switch(job.getStatus()) {
			case COMPLETED:
				onJobCompleted();
				break;
			case FAILED:
				MessageUtil.showError("survey.export.error", Labels.getLabel(job.getErrorMessage(), job.getErrorMessageArgs()));
				break;
			default:
			}
			closeJobStatusPopUp();
		}

		protected void onJobCompleted() {
			MessageUtil.showInfo("survey.export.completed");
		}
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
	
	protected void closeJobStatusPopUp() {
		closePopUp(jobStatusPopUp);
		jobStatusPopUp = null;
	}
	
	@DependsOn({"tempForm.type","tempForm.outputFormat"})
	public boolean isIncludeDataVisible() {
		SurveyType type = SurveyType.valueOf(getTypeFormField());
		OutputFormat outputFormat = OutputFormat.valueOf(getOutputFormatFormField());
		return type == PUBLISHED && outputFormat == RDB;
	}

	public boolean isCollectEarthSurvey() {
		return surveySummary != null && surveySummary.getTarget() == SurveyTarget.COLLECT_EARTH;
	}

	public SurveyExportParametersFormObject getFormObject() {
		return formObject;
	}
	
	public void setFormObject(SurveyExportParametersFormObject formObject) {
		this.formObject = formObject;
	}
	
	public SurveySummary getSurvey() {
		return surveySummary;
	}
	
	public Form getTempForm() {
		return tempForm;
	}
	
	public void setTempForm(Form tempForm) {
		this.tempForm = tempForm;
	}
	
	private void checkEnabledFields() {
		if ( isIncludeDataVisible() ) {
			boolean includeData = getFormFieldValue(tempForm, "includeData");
			if ( ! includeData ) {
				setFormFieldValue(tempForm, "includeUploadedFiles", false);
			}
		} else {
			setFormFieldValue(tempForm, "includeData", false);
		}
	}

	private String getOutputFormatFormField() {
		return getFormFieldValue(tempForm, "outputFormat");
	}

	private String getTypeFormField() {
		return getFormFieldValue(tempForm, "type");
	}
	
	public List<String> getSurveyLanguages() {
		return surveySummary.getLanguages();
	}
	
	public static class SurveyExportParametersFormObject {
		
		public enum OutputFormat {
			MOBILE, DESKTOP, RDB, EARTH
		}
		
		private String type;
		private boolean includeData;
		private boolean includeUploadedFiles;
		private String outputFormat;
		private String rdbDialect;
		private String rdbDateTimeFormat;
		private String rdbTargetSchemaName;
		private String languageCode;
		
		public String getType() {
			return type;
		}
		
		public SurveyType getTypeEnum() {
			return SurveyType.valueOf(type);
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public boolean isIncludeData() {
			return includeData;
		}
		
		public void setIncludeData(boolean includeData) {
			this.includeData = includeData;
		}
		
		public boolean isIncludeUploadedFiles() {
			return includeUploadedFiles;
		}
		
		public void setIncludeUploadedFiles(boolean includeUploadedFiles) {
			this.includeUploadedFiles = includeUploadedFiles;
		}
		
		public String getOutputFormat() {
			return outputFormat;
		}
		
		public OutputFormat getOutputFormatEnum() {
			return OutputFormat.valueOf(outputFormat);
		}
		
		public void setOutputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
		}

		public String getRdbDialect() {
			return rdbDialect;
		}

		public RdbDialect getRdbDialectEnum() {
			return RdbDialect.valueOf(rdbDialect);
		}
		
		public void setRdbDialect(String rdbDialect) {
			this.rdbDialect = rdbDialect;
		}

		public String getRdbDateTimeFormat() {
			return rdbDateTimeFormat;
		}

		public void setRdbDateTimeFormat(String rdbDateTimeFormat) {
			this.rdbDateTimeFormat = rdbDateTimeFormat;
		}

		public String getRdbTargetSchemaName() {
			return rdbTargetSchemaName;
		}
		
		public void setRdbTargetSchemaName(String rdbTargetSchemaName) {
			this.rdbTargetSchemaName = rdbTargetSchemaName;
		}
		
		public String getLanguageCode() {
			return languageCode;
		}
		
		public void setLanguageCode(String languageCode) {
			this.languageCode = languageCode;
		}
	}

}
