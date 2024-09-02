package org.openforis.collect.metamodel.view;

import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;

public class SurveyFileView extends SurveyObjectView {

	private String fileName;
	private SurveyFileType type;

	public SurveyFileView(SurveyFile surveyFile) {
		this.id = surveyFile.getId();
		this.fileName = surveyFile.getFilename();
		this.type = surveyFile.getType();
	}

	public static SurveyFileView fromSurveyFile(SurveyFile surveyFile) {
		SurveyFileView view = new SurveyFileView(surveyFile);
		return view;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public SurveyFileType getType() {
		return type;
	}

	public void setType(SurveyFileType type) {
		this.type = type;
	}

}
