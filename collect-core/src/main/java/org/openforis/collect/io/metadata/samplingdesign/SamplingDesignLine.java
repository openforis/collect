package org.openforis.collect.io.metadata.samplingdesign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openforis.collect.io.metadata.parsing.Line;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignLine extends Line {
	
	private List<String> levelCodes;
	private String x;
	private String y;
	private String srsId;
	private Map<String, String> infos;
	
	public List<String> getLevelCodes() {
		return CollectionUtils.unmodifiableList(levelCodes);
	}
	
	public void setLevelCodes(List<String> levelCodes) {
		this.levelCodes = levelCodes;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String getSrsId() {
		return srsId;
	}

	public void setSrsId(String srsId) {
		this.srsId = srsId;
	}
	
	public boolean hasEqualLocation(SamplingDesignLine other){
		return getX().equals(other.getX()) 
				&& getY().equals(other.getY()) 
				&& getSrsId().equals(other.getSrsId());
	}

	public Map<String, String> getInfos() {
		return CollectionUtils.unmodifiableMap(infos);
	}
	
	public String getInfo(String name) {
		if ( infos == null ) {
			return null;
		} else {
			return infos.get(name);
		}
	}
	
	public void setInfos(Map<String, String> infos) {
		this.infos = infos;
	}
	
	public SamplingDesignItem toSamplingDesignItem(CollectSurvey survey) {
		SamplingDesignItem item = new SamplingDesignItem();
		Integer surveyId = survey.getId();
		if ( survey.isWork() ) {
			item.setSurveyWorkId(surveyId);
		} else {
			item.setSurveyId(surveyId);
		}
		item.setX(Double.parseDouble(x));
		item.setY(Double.parseDouble(y));
		item.setSrsId(srsId);
		item.setLevelCodes(getLevelCodes());
		List<String> infos = new ArrayList<String>();
		String[] columnNames = SamplingDesignFileColumn.INFO_COLUMN_NAMES;
		for (int i = 0; i < columnNames.length; i++) {
			infos.add(getInfo(columnNames[i]));
		}
		item.setInfos(infos);
		return item;
	}

	
}