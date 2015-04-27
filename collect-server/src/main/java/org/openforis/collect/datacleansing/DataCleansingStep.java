package org.openforis.collect.datacleansing;

import java.util.Date;

import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.openforis.idm.metamodel.Survey;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStep extends PersistedSurveyObject {
	
	private static final long serialVersionUID = 1L;
	
	private String title;
	private String description;
	private Date creationDate;
	private Date modifiedDate;
	
	private Integer queryId;
	private String fixExpression;
	private transient DataQuery query;
	
	public DataCleansingStep(Survey survey) {
		super(survey);
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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
	}
	
	public DataQuery getQuery() {
		return query;
	}
	
	public String getFixExpression() {
		return fixExpression;
	}
	
	public void setFixExpression(String fixExpression) {
		this.fixExpression = fixExpression;
	}
	
}
