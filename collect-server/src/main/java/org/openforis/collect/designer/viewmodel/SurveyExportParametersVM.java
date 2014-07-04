package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.model.SurveySummary;
import org.zkoss.bind.BindUtils;
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

	private static final String TEMPORARY_TYPE = "temporary";
	private static final String PUBLISHED_TYPE = "published";
	private static final String MOBILE_OUTPUT_FORMAT = "MOBILE";
	private static final String DESKTOP_OUTPUT_FORMAT = "DESKTOP";
	
	private SurveySummary survey;
	private SurveyExportParametersFormObject formObject;
	private SimpleForm tempForm;
	
	public SurveyExportParametersVM() {
		this.tempForm = new SimpleForm();
	}

	@Init
	public void init(@ExecutionArgParam("survey") SurveySummary survey) {
		this.formObject = new SurveyExportParametersFormObject();
		this.survey = survey;
		this.formObject.setOutputFormat(DESKTOP_OUTPUT_FORMAT);
//		
		if ( this.survey.isOnlyWork() ) {
			this.formObject.setType(TEMPORARY_TYPE);
		} else {
			this.formObject.setType(PUBLISHED_TYPE);
		}
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
	public boolean isIncludeDataDisabled() {
		String type = getTypeFormField();
		String outputFormat = getOutputFormatFormField();
		return TEMPORARY_TYPE.equals(type) || 
				MOBILE_OUTPUT_FORMAT.equals(outputFormat);
	}

	@DependsOn("tempForm.includeData")
	public boolean isIncludeUploadedFilesDisabled() {
		Boolean includeData = (Boolean) tempForm.getField("includeData");
		return includeData == null || ! includeData.booleanValue();
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
	
	public SimpleForm getTempForm() {
		return tempForm;
	}
	
	public void setTempForm(SimpleForm tempForm) {
		this.tempForm = tempForm;
	}
	
	private void checkEnabledFields() {
		String type = getTypeFormField();
		String outputFormat = getOutputFormatFormField();
		if ( TEMPORARY_TYPE.equals(type) || MOBILE_OUTPUT_FORMAT.equals(outputFormat) ) {
			tempForm.setField("includeData", false);
			BindUtils.postNotifyChange(null, null, tempForm, "includeData");
		}
		
		boolean includeData = (Boolean) tempForm.getField("includeData");
		if ( ! includeData ) {
			tempForm.setField("includeUploadedFiles", false);
			BindUtils.postNotifyChange(null, null, tempForm, "includeUploadedFiles");
		}
	}

	private String getOutputFormatFormField() {
		String outputFormat = (String) tempForm.getField("outputFormat");
		return outputFormat;
	}

	private String getTypeFormField() {
		String type = (String) tempForm.getField("type");
		return type;
	}
	
	public static class SurveyExportParametersFormObject {
		
		private String type;
		private boolean includeData;
		private boolean includeUploadedFiles;
		private String outputFormat;
		
		public String getType() {
			return type;
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
		
		public void setOutputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
		}

	}

	
}
