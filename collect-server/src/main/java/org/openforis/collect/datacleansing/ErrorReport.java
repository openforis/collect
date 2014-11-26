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
public class ErrorReport extends PersistedObject {
	
	private Query query;
	private Date creationDate;	
	private List<ErrorReportItem> items;
	
	public void addItem(ErrorReportItem item) {
		if (items == null) {
			items = new ArrayList<ErrorReportItem>();
		}
		items.add(item);
	}
	
	public void removeItem(ErrorReportItem item) {
		items.remove(item);
	}
	
	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public List<ErrorReportItem> getItems() {
		return items;
	}

	public void setItems(List<ErrorReportItem> items) {
		this.items = items;
	}

}
