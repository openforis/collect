package org.openforis.collect.datacleansing;

import java.util.UUID;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;


/**
 * 
 * @author A. Modragon
 *
 */
public class DataErrorQuery extends PersistedSurveyObject {

	private static final long serialVersionUID = 1L;
	
	private Integer queryId;
	private DataQuery query;
	private Integer typeId;
	private DataErrorType type;
	
	public DataErrorQuery(CollectSurvey survey) {
		super(survey);
	}

	public DataErrorQuery(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}

	public Integer getQueryId() {
		return query == null ? queryId: query.getId();
	}
	
	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
		this.query = null;
	}
	
	public DataQuery getQuery() {
		return query;
	}
	
	public void setQuery(DataQuery query) {
		this.query = query;
		this.queryId = query == null ? null: query.getId();
	}
	
	public Integer getTypeId() {
		return type == null ? typeId: type.getId();
	}
	
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
		this.type = null;
	}
	
	public DataErrorType getType() {
		return type;
	}
	
	public void setType(DataErrorType type) {
		this.type = type;
		this.typeId = type == null ? null: type.getId();
	}
	
}
