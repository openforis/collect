package org.openforis.collect.model;

import org.openforis.idm.model.species.Taxonomy;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectTaxonomy extends Taxonomy {

	private Integer surveyId;
	private Integer surveyWorkId;
	
	public Integer getSurveyId() {
		return surveyId;
	}
	
	public void setSurveyId(Integer surveyId) {
		this.surveyId = surveyId;
	}
	
	public Integer getSurveyWorkId() {
		return surveyWorkId;
	}
	
	public void setSurveyWorkId(Integer surveyWorkId) {
		this.surveyWorkId = surveyWorkId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((surveyId == null) ? 0 : surveyId.hashCode());
		result = prime * result
				+ ((surveyWorkId == null) ? 0 : surveyWorkId.hashCode());
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
		CollectTaxonomy other = (CollectTaxonomy) obj;
		if (surveyId == null) {
			if (other.surveyId != null)
				return false;
		} else if (!surveyId.equals(other.surveyId))
			return false;
		if (surveyWorkId == null) {
			if (other.surveyWorkId != null)
				return false;
		} else if (!surveyWorkId.equals(other.surveyWorkId))
			return false;
		return true;
	}
	
}
