package org.openforis.collect.manager;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.io.File;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreator;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreatorImpl;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_PROTOTYPE)
public class CollectEarthSurveyExportJob extends Job {
	
	protected static final ServiceLoader<CollectEarthProjectFileCreator> COLLECT_EARTH_PROJECT_FILE_CREATOR_LOADER = 
			ServiceLoader.load(CollectEarthProjectFileCreator.class);
	private static final CollectEarthProjectFileCreator COLLECT_EARTH_PROJECT_FILE_CREATOR;
	static {
		Iterator<CollectEarthProjectFileCreator> it = COLLECT_EARTH_PROJECT_FILE_CREATOR_LOADER.iterator();
		COLLECT_EARTH_PROJECT_FILE_CREATOR = it.hasNext() ? it.next(): null;
	}
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CodeListManager codeListManager;

	//input
	private CollectSurvey survey;
	private String languageCode;
	
	//output
	private File outputFile;

	@Override
	protected void buildTasks() throws Throwable {
		addTask(new ExportTask());
	}
	
	private class ExportTask extends Task {
		
		@Override
		protected void execute() throws Throwable {
			CollectEarthProjectFileCreatorImpl creatorImpl = (CollectEarthProjectFileCreatorImpl) COLLECT_EARTH_PROJECT_FILE_CREATOR;
			creatorImpl.setCodeListManager(codeListManager);
			creatorImpl.setSurveyManager(surveyManager);
			outputFile = COLLECT_EARTH_PROJECT_FILE_CREATOR.create(survey, languageCode);
		}
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	
	public File getOutputFile() {
		return outputFile;
	}

}
