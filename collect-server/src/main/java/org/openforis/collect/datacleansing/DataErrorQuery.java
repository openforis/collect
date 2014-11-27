package org.openforis.collect.datacleansing;

import org.openforis.collect.model.CollectSurvey;


/**
 * 
 * @author A. Modragon
 *
 */
public class DataErrorQuery extends DataQuery {

	private static final long serialVersionUID = 1L;
	
	private Integer typeId;
	private DataErrorType type;
	
	public DataErrorQuery(CollectSurvey survey) {
		super(survey);
	}

	public Integer getTypeId() {
		return type == null ? typeId: type.getId();
	}
	
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	
	public DataErrorType getType() {
		return type;
	}
	
	public void setType(DataErrorType type) {
		this.type = type;
		this.typeId = type == null ? null: type.getId();
	}
	
}
