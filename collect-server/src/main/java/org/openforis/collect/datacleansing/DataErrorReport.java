package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openforis.idm.metamodel.PersistedObject;

/**
 * 
 * @author A. Modragon
 *
 */
public class DataErrorReport extends PersistedObject {
	
	private int queryId;
	private DataQuery query;
	private Date creationDate;	
	private List<DataErrorReportItem> items;
	
	public DataErrorReport() {
		creationDate = new Date();
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
	
	public DataQuery getQuery() {
		return query;
	}

	public void setQuery(DataQuery query) {
		this.query = query;
		this.queryId = query.getId();
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public List<DataErrorReportItem> getItems() {
		return items;
	}

	public void setItems(List<DataErrorReportItem> items) {
		this.items = items;
	}

}
