package org.openforis.collect.io.metadata;

import java.io.OutputStream;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.xml.internal.marshal.SurveyMarshaller.SurveyMarshalParameters;
import org.springframework.beans.factory.annotation.Autowired;
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
public class IdmlExportTask extends Task {
	
	@Autowired
	private SurveyManager surveyManager;
	
	//parameters
	private CollectSurvey survey;
	private String outputSurveyDefaultLanguage;
	private OutputStream outputStream;

	public IdmlExportTask() {
	}
	
	@Override
	protected void execute() throws Throwable {
		surveyManager.marshalSurvey(survey, outputStream,
				new SurveyMarshalParameters(true, true, false, outputSurveyDefaultLanguage));
	}
	
	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public void setOutputSurveyDefaultLanguage(String outputSurveyDefaultLanguage) {
		this.outputSurveyDefaultLanguage = outputSurveyDefaultLanguage;
	}
	
}