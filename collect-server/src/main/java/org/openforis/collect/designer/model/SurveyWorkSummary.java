package org.openforis.collect.designer.model;

import org.openforis.collect.model.SurveySummary;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyWorkSummary extends SurveySummary {
	
	private boolean working;
	private boolean published;
	private Integer publishedSurveyId;
	
	public SurveyWorkSummary(Integer id, String name, String uri,
			boolean published, boolean working) {
		super(id, name, uri);
		this.published = published;
		this.working = working;
	}

	public boolean isPublished() {
		return published;
	}
	
	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isWorking() {
		return working;
	}
	
	public void setWorking(boolean working) {
		this.working = working;
	}

	public Integer getPublishedSurveyId() {
		return publishedSurveyId;
	}

	public void setPublishedSurveyId(Integer publishedSurveyId) {
		this.publishedSurveyId = publishedSurveyId;
	}

}
