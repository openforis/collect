package org.openforis.collect.datacleansing;

import org.openforis.collect.model.CollectSurvey;


/**
 * 
 * @author A. Modragon
 *
 */
public class ErrorQuery extends Query {

	private ErrorType type;
	
	public ErrorQuery(CollectSurvey survey) {
		super(survey);
	}

	public ErrorType getType() {
		return type;
	}
	
	public void setType(ErrorType type) {
		this.type = type;
	}
	
}
