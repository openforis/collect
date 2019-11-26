package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQueryGroup extends DataCleansingItem {

	private static final long serialVersionUID = 1L;
	
	private String title;
	private String description;
	private List<Integer> queryIds = new ArrayList<Integer>();
	
	private transient List<DataQuery> queries = new ArrayList<DataQuery>();
	
	public DataQueryGroup(CollectSurvey survey) {
		super(survey);
	}

	public DataQueryGroup(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}
	
	public DataQuery getQuery(int id) {
		DataQuery query = CollectionUtils.findItem(queries, id);
		return query;
	}
	
	public void addQuery(DataQuery query) {
		this.queries.add(query);
		this.queryIds.add(query.getId());
	}
	
	public void removeAllQueries() {
		this.queries.clear();
		this.queryIds.clear();
	}
	
	public void allAllQueries(List<DataQuery> queries) {
		for (DataQuery query : queries) {
			addQuery(query);
		}
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

	public List<DataQuery> getQueries() {
		return queries;
	}
	
	public void setQueries(List<DataQuery> queries) {
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
		DataQueryGroup other = (DataQueryGroup) obj;
		if (! CollectionUtils.<DataQuery>deepEquals(queries, other.queries, true))
			return false;
		return true;
	}

}
