package org.openforis.collect.datacleansing.json;

import java.io.File;
import java.util.UUID;

import org.openforis.collect.datacleansing.DataCleansingMetadata;
import org.openforis.collect.datacleansing.DataCleansingMetadataView;
import org.openforis.collect.datacleansing.io.DataCleansingExportTask;
import org.openforis.collect.datacleansing.manager.DataCleansingMetadataManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonDataCleansingExportTask extends Task implements DataCleansingExportTask {
	
	@Autowired
	private DataCleansingMetadataManager dataCleansingManager;
	
	//input
	private CollectSurvey survey;
	//output
	private File resultFile;
	
	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		resultFile = File.createTempFile("datacleansing_metadata", ".json");
	}
	
	@Override
	protected void execute() throws Throwable {
		DataCleansingMetadata metadata = dataCleansingManager.loadMetadata(survey);
		if (! metadata.isEmpty()) {
			DataCleansingMetadataView metadataView = DataCleansingMetadataView.fromMetadata(metadata);
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(resultFile, metadataView);
		}
	}
	
	@Override
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	@Override
	public File getResultFile() {
		return resultFile;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(UUID.randomUUID().toString());
	}
	
}