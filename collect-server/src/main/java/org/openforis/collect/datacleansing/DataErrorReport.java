package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * 
 * @author A. Modragon
 *
 */
public class DataErrorReport extends PersistedSurveyObject {
	
	private static final long serialVersionUID = 1L;
	
	private int queryId;
	private DataErrorQuery query;
	private List<DataErrorReportItem> items;
	
	public DataErrorReport(CollectSurvey survey) {
		super(survey);
	}
	
	public DataErrorReport(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
		items = new ArrayList<DataErrorReportItem>();
	}

	public void addItem(DataErrorReportItem item) {
		items.add(item);
	}
	
	public void removeItem(DataErrorReportItem item) {
		items.remove(item);
	}
	
	public int getQueryId() {
		return queryId;
	}
	
	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}
	
	public DataErrorQuery getQuery() {
		return query;
	}

	public void setQuery(DataErrorQuery query) {
		this.query = query;
		this.queryId = query.getId();
	}
	
	public List<DataErrorReportItem> getItems() {
		return items;
	}

	public void setItems(List<DataErrorReportItem> items) {
		this.items = items;
	}

}
