package org.openforis.collect.model;

import org.openforis.idm.model.species.Taxonomy;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectTaxonomy extends Taxonomy {

	private CollectSurvey survey;
	private Integer surveyId;
	
	public CollectTaxonomy() {
	}
	
	public CollectTaxonomy(CollectTaxonomy source) {
		super(source);
		this.survey = source.survey;
		this.surveyId = source.surveyId;
	}
	
	public Integer getSurveyId() {
		return surveyId;
	}
	
	public void setSurveyId(Integer surveyId) {
		this.surveyId = surveyId;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
		this.surveyId = survey == null ? null : survey.getId();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((surveyId == null) ? 0 : surveyId.hashCode());
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
		return true;
	}
	
}
