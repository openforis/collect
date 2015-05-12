/**
 * 
 */
package org.openforis.collect.io.metadata;

import java.io.File;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IdmlUnmarshallTask extends Task {

	private SurveyManager surveyManager;
	
	//input
	private File file;
	private boolean validate;

	//output
	private CollectSurvey survey;

	@Override
	protected void execute() throws Throwable {
		this.survey = this.surveyManager.unmarshalSurvey(file, validate, false);
	}
	
	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}

	public boolean isValidate() {
		return validate;
	}
	
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}
	
}
