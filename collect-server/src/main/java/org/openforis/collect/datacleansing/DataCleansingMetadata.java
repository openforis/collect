package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.lang.DeepComparable;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingMetadata implements DeepComparable {
	
	private CollectSurvey survey;
	private List<DataQuery> dataQueries;
	private List<DataErrorType> dataErrorTypes;
	private List<DataErrorQuery> dataErrorQueries;
	private List<DataErrorQueryGroup> dataErrorQueryGroups;
	private List<DataCleansingStep> cleansingSteps;
	private List<DataCleansingChain> cleansingChains;

	public DataCleansingMetadata(CollectSurvey survey) {
		super();
		this.survey = survey;
		this.dataQueries = new ArrayList<DataQuery>();
		this.dataErrorTypes = new ArrayList<DataErrorType>();
		this.dataErrorQueries = new ArrayList<DataErrorQuery>();
		this.dataErrorQueryGroups = new ArrayList<DataErrorQueryGroup>();
		this.cleansingSteps = new ArrayList<DataCleansingStep>();
		this.cleansingChains = new ArrayList<DataCleansingChain>();
	}
	
	public DataCleansingMetadata(
			CollectSurvey survey,
			List<DataQuery> dataQueries,
			List<DataErrorType> dataErrorTypes,
			List<DataErrorQuery> dataErrorQueries,
			List<DataErrorQueryGroup> dataErrorQueryGroups,
			List<DataCleansingStep> cleansingSteps,
			List<DataCleansingChain> cleansingChains) {
		super();
		this.survey = survey;
		this.dataQueries = dataQueries;
		this.dataErrorTypes = dataErrorTypes;
		this.dataErrorQueries = dataErrorQueries;
		this.dataErrorQueryGroups = dataErrorQueryGroups;
		this.cleansingSteps = cleansingSteps;
		this.cleansingChains = cleansingChains;
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataCleansingMetadata other = (DataCleansingMetadata) obj;
		if (! CollectionUtils.<DataQuery>deepEquals(dataQueries, other.dataQueries, true))
			return false;
		if (! CollectionUtils.<DataErrorType>deepEquals(dataErrorTypes, other.dataErrorTypes, true))
			return false;
		if (! CollectionUtils.<DataErrorQuery>deepEquals(dataErrorQueries, other.dataErrorQueries, true))
			return false;
		if (! CollectionUtils.<DataErrorQueryGroup>deepEquals(dataErrorQueryGroups, other.dataErrorQueryGroups, true))
			return false;
		if (! CollectionUtils.<DataCleansingStep>deepEquals(cleansingSteps, other.cleansingSteps, true))
			return false;
		if (! CollectionUtils.<DataCleansingChain>deepEquals(cleansingChains, other.cleansingChains, true))
			return false;
		
		//do not deep compare surveys
		if (survey == null) {
			if (other.survey != null)
				return false;
		} else if (!survey.equals(other.survey))
			return false;
		return true;
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
	
	public List<DataErrorQueryGroup> getDataErrorQueryGroups() {
		return dataErrorQueryGroups;
	}
	
	public List<DataCleansingStep> getCleansingSteps() {
		return cleansingSteps;
	}

	public List<DataCleansingChain> getCleansingChains() {
		return cleansingChains;
	}
	
}