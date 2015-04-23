package org.openforis.collect.datacleansing;

import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.openforis.idm.metamodel.Survey;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStep extends PersistedSurveyObject {
	
	private static final long serialVersionUID = 1L;
	
	private Integer queryId;
	private String fixExpression;
	private transient DataQuery query;
	
	public DataCleansingStep(Survey survey) {
		super(survey);
	}
	
	public Integer getQueryId() {
		return queryId;
	}
	
	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
	}
	
	public DataQuery getQuery() {
		return query;
	}
	
	public void setQuery(DataQuery query) {
		this.query = query;
	}
	
	public String getFixExpression() {
		return fixExpression;
	}
	
	public void setFixExpression(String fixExpression) {
		this.fixExpression = fixExpression;
	}
	
}
