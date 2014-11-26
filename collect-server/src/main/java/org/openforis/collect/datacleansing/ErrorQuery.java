package org.openforis.collect.datacleansing;

import org.openforis.collect.model.CollectSurvey;


/**
 * 
 * @author A. Modragon
 *
 */
public class ErrorQuery extends Query {

	private static final long serialVersionUID = 1L;
	
	private Integer typeId;
	private ErrorType type;
	
	public ErrorQuery(CollectSurvey survey) {
		super(survey);
	}

	public Integer getTypeId() {
		return typeId;
	}
	
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	
	public ErrorType getType() {
		return type;
	}
	
	public void setType(ErrorType type) {
		this.type = type;
	}
	
}
