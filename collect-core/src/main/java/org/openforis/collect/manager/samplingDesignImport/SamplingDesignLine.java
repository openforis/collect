package org.openforis.collect.manager.samplingDesignImport;

import java.util.List;

import org.openforis.collect.manager.referenceDataImport.Line;
import org.openforis.idm.util.CollectionUtil;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignLine extends Line {
	
	private List<String> levelCodes;
	private String location;
	
	public List<String> getLevelCodes() {
		return CollectionUtil.unmodifiableList(levelCodes);
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