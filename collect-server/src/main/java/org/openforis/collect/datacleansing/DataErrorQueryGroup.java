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
public class DataErrorQueryGroup extends PersistedSurveyObject {

	private static final long serialVersionUID = 1L;
	
	private List<Integer> queryIds = new ArrayList<Integer>();
	
	private transient List<DataErrorQuery> queries = new ArrayList<DataErrorQuery>();
	
	public DataErrorQueryGroup(CollectSurvey survey) {
		super(survey);
	}

	public DataErrorQueryGroup(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}
	
	public List<DataErrorQuery> getQueries() {
		return queries;
	}
	
	public void setQueries(List<DataErrorQuery> queries) {
		this.queries = queries;
	}
	
	public List<Integer> getQueryIds() {
		return queryIds;
	}
	
	public void setQueryIds(List<Integer> queryIds) {
		this.queryIds = queryIds;
	}
	
	@Override
	public boolean deepEquals(Object obj, boolean ignoreId) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj, ignoreId))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataErrorQueryGroup other = (DataErrorQueryGroup) obj;
		if (queryIds == null) {
			if (other.queryIds != null)
				return false;
		} else if (!queryIds.equals(other.queryIds))
			return false;
		return true;
	}

}
