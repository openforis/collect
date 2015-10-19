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
	private List<DataQueryType> dataQueryTypes;
	private List<DataQuery> dataQueries;
	private List<DataQueryGroup> dataQueryGroups;
	private List<DataCleansingStep> cleansingSteps;
	private List<DataCleansingChain> cleansingChains;

	public DataCleansingMetadata(CollectSurvey survey) {
		super();
		this.survey = survey;
		this.dataQueries = new ArrayList<DataQuery>();
		this.dataQueryTypes = new ArrayList<DataQueryType>();
		this.dataQueryGroups = new ArrayList<DataQueryGroup>();
		this.cleansingSteps = new ArrayList<DataCleansingStep>();
		this.cleansingChains = new ArrayList<DataCleansingChain>();
	}
	
	public DataCleansingMetadata(
			CollectSurvey survey,
			List<DataQueryType> dataQueryTypes,
			List<DataQuery> dataQueries,
			List<DataQueryGroup> dataQueryGroups,
			List<DataCleansingStep> cleansingSteps,
			List<DataCleansingChain> cleansingChains) {
		super();
		this.survey = survey;
		this.dataQueries = dataQueries;
		this.dataQueryTypes = dataQueryTypes;
		this.dataQueryGroups = dataQueryGroups;
		this.cleansingSteps = cleansingSteps;
		this.cleansingChains = cleansingChains;
	}
	
	public boolean isEmpty() {
		return 	org.apache.commons.collections.CollectionUtils.isEmpty(dataQueries) &&
				org.apache.commons.collections.CollectionUtils.isEmpty(dataQueryTypes) &&
				org.apache.commons.collections.CollectionUtils.isEmpty(dataQueryGroups) &&
				org.apache.commons.collections.CollectionUtils.isEmpty(cleansingSteps) &&
				org.apache.commons.collections.CollectionUtils.isEmpty(cleansingChains);
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
		if (! CollectionUtils.<DataQueryType>deepEquals(dataQueryTypes, other.dataQueryTypes, true))
			return false;
		if (! CollectionUtils.<DataQuery>deepEquals(dataQueries, other.dataQueries, true))
			return false;
		if (! CollectionUtils.<DataQueryGroup>deepEquals(dataQueryGroups, other.dataQueryGroups, true))
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
	
	public List<DataQueryType> getDataQueryTypes() {
		return dataQueryTypes;
	}
	
	public List<DataQueryGroup> getDataQueryGroups() {
		return dataQueryGroups;
	}
	
	public List<DataCleansingStep> getCleansingSteps() {
		return cleansingSteps;
	}

	public List<DataCleansingChain> getCleansingChains() {
		return cleansingChains;
	}
	
}