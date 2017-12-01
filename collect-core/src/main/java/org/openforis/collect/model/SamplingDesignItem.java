package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignItem {

	private Integer id;
	private Integer surveyId;
	private List<String> levelCodes;
	private String srsId;
	private Double x;
	private Double y;
	private List<String> infoAttributes;
	
	public int getLevel() {
		return levelCodes == null ? -1 : levelCodes.size();
	}
	
	public void addLevelCode(String code) {
		if ( levelCodes == null ) {
			levelCodes = new ArrayList<String>();
		}
		levelCodes.add(code);
	}
	
	public String getLevelCode(int level) {
		if ( levelCodes == null || level <= 0 || level > levelCodes.size()) {
			throw new IllegalArgumentException("Level " + level + " is not accessible");
		} else {
			return levelCodes.get(level - 1);
		}
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

	public List<String> getLevelCodes() {
		return CollectionUtils.unmodifiableList(levelCodes);
	}

	public void setLevelCodes(List<String> levelCodes) {
		this.levelCodes = levelCodes;
	}
	
	public String getSrsId() {
		return srsId;
	}

	public void setSrsId(String srsId) {
		this.srsId = srsId;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public void addInfoAttribute(String info) {
		if ( infoAttributes == null ) {
			infoAttributes = new ArrayList<String>();
		}
		infoAttributes.add(info);
	}
	
	public String getInfoAttribute(int index) {
		if ( infoAttributes == null ) {
			return null;
		} else {
			return infoAttributes.get(index);
		}
	}
	
	public List<String> getInfoAttributes() {
		return CollectionUtils.unmodifiableList(infoAttributes);
	}

	public void setInfoAttributes(List<String> infos) {
		this.infoAttributes = infos;
	}

	@Override
	public String toString() {
		return levelCodes + " srsId=" + srsId +", x=" + x + ", y=" + y;
	}
	
	
}
