package org.openforis.collect.datacleansing;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;

public class DataCleansingMetadata {
	
	private CollectSurvey survey;
	private List<DataQuery> dataQueries;
	private List<DataErrorType> dataErrorTypes;
	private List<DataErrorQuery> dataErrorQueries;
	private List<DataCleansingStep> cleansingSteps;
	private List<DataCleansingChain> cleansingChains;

	public DataCleansingMetadata(
			CollectSurvey survey,
			List<DataQuery> dataQueries,
			List<DataErrorType> dataErrorTypes,
			List<DataErrorQuery> dataErrorQueries,
			List<DataCleansingStep> cleansingSteps,
			List<DataCleansingChain> cleansingChains) {
		super();
		this.survey = survey;
		this.dataQueries = dataQueries;
		this.dataErrorTypes = dataErrorTypes;
		this.dataErrorQueries = dataErrorQueries;
		this.cleansingSteps = cleansingSteps;
		this.cleansingChains = cleansingChains;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public List<DataQuery> getDataQueries() {
		return dataQueries;
	}
	
	public List<DataErrorType> getDataErrorTypes() {
		return dataErrorTypes;
	}
	
	public List<DataErrorQuery> getDataErrorQueries() {
		return dataErrorQueries;
	}
	
	public List<DataCleansingStep> getCleansingSteps() {
		return cleansingSteps;
	}

	public List<DataCleansingChain> getCleansingChains() {
		return cleansingChains;
	}
	
}