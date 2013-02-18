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
	private String srsId;
	private String latitude;
	private String longitude;
	
	public List<String> getLevelCodes() {
		return CollectionUtil.unmodifiableList(levelCodes);
	}
	
	public String getLocation() {
		StringBuilder sb = new StringBuilder();
		sb.append("SRID=");
		sb.append(srsId);
		sb.append(";");
		sb.append("POINT(");
		sb.append(latitude);
		sb.append(" ");
		sb.append(longitude);
		sb.append(")");
		return sb.toString();
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

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
}