package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.model.Coordinate;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignItem {

	private Integer id;
	private Integer surveyId;
	private List<String> levelCodes = new ArrayList<String>();
	private String srsId;
	private Double x;
	private Double y;
	private List<String> infoAttributes = new ArrayList<String>();
	
	public int getLevel() {
		return levelCodes.size();
	}
	
	public void addLevelCode(String code) {
		levelCodes.add(code);
	}
	
	public String getLevelCode(int level) {
		if ( level <= 0 || level > levelCodes.size()) {
			throw new IllegalArgumentException("Level " + level + " is not accessible");
		} else {
			return levelCodes.get(level - 1);
		}
	}
	
	public Coordinate getCoordinate() {
		return new Coordinate(x, y, srsId);
	}
	
	public void setCoordinate(Coordinate c) {
		this.x = c.getX();
		this.y = c.getY();
		this.srsId = c.getSrsId();
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
		infoAttributes.add(info);
	}
	
	public String getInfoAttribute(int index) {
		if (index > infoAttributes.size()) {
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
