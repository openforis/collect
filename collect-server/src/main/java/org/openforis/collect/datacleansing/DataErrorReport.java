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
	
	private DataQuery query;
	private Date creationDate;	
	private List<DataErrorReportItem> items;
	
	public DataErrorReport() {
		items = new ArrayList<DataErrorReportItem>();
	}
	
	public void addItem(DataErrorReportItem item) {
		items.add(item);
	}
	
	public void removeItem(DataErrorReportItem item) {
		items.remove(item);
	}
	
	public DataQuery getQuery() {
		return query;
	}

	public void setQuery(DataQuery query) {
		this.query = query;
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
