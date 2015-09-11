package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStep extends PersistedSurveyObject {
	
	private static final long serialVersionUID = 1L;
	
	private String title;
	private String description;
	private Integer queryId;
	private List<DataCleansingStepValue> udpateValues = new ArrayList<DataCleansingStepValue>();
	
	private transient DataQuery query;
	
	public DataCleansingStep(CollectSurvey survey) {
		super(survey);
	}
	
	public DataCleansingStep(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}
	
	public void addUpdateValue(DataCleansingStepValue updateValue) {
		if (this.udpateValues == null) {
			this.udpateValues = new ArrayList<DataCleansingStepValue>();
		}
		this.udpateValues.add(updateValue);
	}

	public Integer getQueryId() {
		return query == null ? queryId : query.getId();
	}
	
	public void setQuery(DataQuery query) {
		this.query = query;
		this.queryId = query == null ? null: query.getId();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
		this.query = null;
	}
	
	public DataQuery getQuery() {
		return query;
	}
	
	public List<DataCleansingStepValue> getUpdateValues() {
		return udpateValues;
	}
	
	public void setUpdateValues(List<DataCleansingStepValue> values) {
		this.udpateValues = values;
	}

	@Override
	public boolean deepEquals(Object obj, boolean ignoreId) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj, ignoreId))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataCleansingStep other = (DataCleansingStep) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.deepEquals(other.query, ignoreId))
			return false;
		if (! ignoreId) {
			if (queryId == null) {
				if (other.queryId != null)
					return false;
			} else if (!queryId.equals(other.queryId))
				return false;
		}
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

}
