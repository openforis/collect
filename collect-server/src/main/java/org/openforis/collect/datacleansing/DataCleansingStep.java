package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;

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
	private List<String> fieldFixExpressions = new ArrayList<String>();
	
	private transient DataQuery query;
	
	public DataCleansingStep(CollectSurvey survey) {
		super(survey);
	}
	
	public DataCleansingStep(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}

	public UpdateType getUpdateType() {
		return StringUtils.isNotBlank(fixExpression) ? UpdateType.ATTRIBUTE : UpdateType.FIELD;
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
		this.query = null;
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

	@Override
	public boolean deepEquals(Object obj, boolean ignoreId) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj, ignoreId))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataCleansingStep other = (DataCleansingStep) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (fieldFixExpressions == null) {
			if (other.fieldFixExpressions != null)
				return false;
		} else if (!fieldFixExpressions.equals(other.fieldFixExpressions))
			return false;
		if (fixExpression == null) {
			if (other.fixExpression != null)
				return false;
		} else if (!fixExpression.equals(other.fixExpression))
			return false;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.deepEquals(other.query, ignoreId))
			return false;
		if (! ignoreId) {
			if (queryId == null) {
				if (other.queryId != null)
					return false;
			} else if (!queryId.equals(other.queryId))
				return false;
		}
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
	
}
