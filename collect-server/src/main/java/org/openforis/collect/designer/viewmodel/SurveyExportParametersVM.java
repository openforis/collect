package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.OutputFormat.DESKTOP;
import static org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.OutputFormat.EARTH;
import static org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.OutputFormat.RDB;
import static org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType.PUBLISHED;
import static org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType.TEMPORARY;
import static org.openforis.collect.metamodel.SurveyTarget.COLLECT_EARTH;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.viewmodel.SurveyExportParametersVM.SurveyExportParametersFormObject.OutputFormat;
import org.openforis.collect.designer.viewmodel.SurveyBaseVM.SurveyType;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.relational.print.RDBPrintJob.RdbDialect;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyExportParametersVM extends BaseVM {
	
	private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private SurveySummary survey;
	private SurveyExportParametersFormObject formObject;
	private Form tempForm;
	
	@Init
	public void init(@ExecutionArgParam("survey") SurveySummary survey) {
		this.survey = survey;
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
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("parameters", formObject);
		BindUtils.postGlobalCommand(null, null, "performSelectedSurveyExport", args);
	}
	
	@DependsOn({"tempForm.type","tempForm.outputFormat"})
	public boolean isIncludeDataVisible() {
		SurveyType type = SurveyType.valueOf(getTypeFormField());
		OutputFormat outputFormat = OutputFormat.valueOf(getOutputFormatFormField());
		return type == PUBLISHED && outputFormat == RDB;
	}

	public SurveyExportParametersFormObject getFormObject() {
		return formObject;
	}
	
	public void setFormObject(SurveyExportParametersFormObject formObject) {
		this.formObject = formObject;
	}
	
	public SurveySummary getSurvey() {
		return survey;
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
		return survey.getLanguages();
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
