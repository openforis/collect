/**
 * 
 */
package org.openforis.collect.datacleansing;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * @author A. Modragon
 *
 */
public class ErrorType extends SurveyObject {

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String code;
	private String label;
	private String description;
	
	public ErrorType(CollectSurvey survey) {
		super(survey);
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ErrorType other = (ErrorType) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
