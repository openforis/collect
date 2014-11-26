/**
 * 
 */
package org.openforis.collect.datacleansing;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * @author A. Modragon
 *
 */
public class ErrorType extends PersistedSurveyObject {

	private static final long serialVersionUID = 1L;
	
	private String code;
	private String label;
	private String description;
	
	public ErrorType(CollectSurvey survey) {
		super(survey);
	}

	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
}
