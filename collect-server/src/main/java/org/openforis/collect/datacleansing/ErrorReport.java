package org.openforis.collect.datacleansing;

import java.util.Date;

/**
 * 
 * @author A. Modragon
 *
 */
public class ErrorReport {
	
	private Integer id;
	private Query query;
	private Date creationDate;	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

}
