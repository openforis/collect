package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignItem {

	private Integer id;
	private Integer surveyId;
	private Integer surveyWorkId;
	private List<String> levelCodes;
	private String location;

	public void addLevelCode(String code) {
		if ( levelCodes == null ) {
			levelCodes = new ArrayList<String>();
		}
		levelCodes.add(code);
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
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

	public List<String> getLevelCodes() {
		return levelCodes;
	}

	public void setLevelCodes(List<String> levelCodes) {
		this.levelCodes = levelCodes;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
}
