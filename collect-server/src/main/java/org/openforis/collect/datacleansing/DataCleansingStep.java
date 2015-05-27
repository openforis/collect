package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openforis.idm.metamodel.PersistedSurveyObject;
import org.openforis.idm.metamodel.Survey;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStep extends PersistedSurveyObject {
	
	private static final long serialVersionUID = 1L;
	
	public enum UpdateType {
		ATTRIBUTE, FIELD
	}
	
	private String title;
	private String description;
	private Integer queryId;
	private String fixExpression;
	private List<String> fieldFixExpressions;
	
	private transient DataQuery query;
	
	public DataCleansingStep(Survey survey) {
		super(survey);
		fieldFixExpressions = new ArrayList<String>();
	}
	
	public UpdateType getUpdateType() {
		return CollectionUtils.isNotEmpty(fieldFixExpressions) ? UpdateType.FIELD: UpdateType.ATTRIBUTE;
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
	
	public List<String> getFieldFixExpressions() {
		return fieldFixExpressions;
	}
	
	public void setFieldFixExpressions(List<String> fieldFixExpressions) {
		this.fieldFixExpressions = fieldFixExpressions;
	}
	
}
