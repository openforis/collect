package org.openforis.collect.io.internal;

import java.io.OutputStream;

import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SurveyBackupInfoCreatorTask extends Task {

	private OutputStream outputStream;
	private CollectSurvey survey;
	
	@Override
	protected void execute() throws Throwable {
		SurveyBackupInfo info = new SurveyBackupInfo();
		info.setSurveyUri(survey.getUri());
		info.setSurveyName(survey.getName());
		info.store(outputStream);
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
}
