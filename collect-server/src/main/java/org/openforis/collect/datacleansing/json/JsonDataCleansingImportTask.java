package org.openforis.collect.datacleansing.json;

import java.io.File;

import org.openforis.collect.datacleansing.DataCleansingMetadata;
import org.openforis.collect.datacleansing.DataCleansingMetadataView;
import org.openforis.collect.datacleansing.io.DataCleansingImportTask;
import org.openforis.collect.datacleansing.manager.DataCleansingMetadataManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonDataCleansingImportTask extends Task implements DataCleansingImportTask {

	@Autowired
	private DataCleansingMetadataManager dataCleansingManager;
	
	//input
	private CollectSurvey survey;
	private File inputFile;
	private User activeUser;
	
	@Override
	protected void execute() throws Throwable {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		DataCleansingMetadataView metadataView = objectMapper.readValue(inputFile, DataCleansingMetadataView.class);
		DataCleansingMetadata metadata = metadataView.toMetadata(survey);
		dataCleansingManager.saveMetadata(survey, metadata, true, activeUser); //TODO handle exceptions
	}

	@Override
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	@Override
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	
	@Override
	public void setActiveUser(User activeUser) {
		this.activeUser = activeUser;
	}
	
}
