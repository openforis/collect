package org.openforis.collect.manager.samplingdesignimport;

import java.util.List;

import org.openforis.collect.manager.referencedataimport.Line;
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
	
	public boolean hasEqualsLocation(SamplingDesignLine other){
		return getX().equals(other.getX()) 
				&& getY().equals(other.getY()) 
				&& getSrsId().equals(other.getSrsId());
	}

}