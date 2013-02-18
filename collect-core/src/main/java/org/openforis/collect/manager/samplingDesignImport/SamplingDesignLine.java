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
	private String x;
	private String y;
	
	public List<String> getLevelCodes() {
		return CollectionUtil.unmodifiableList(levelCodes);
	}
	
	public String getLocation() {
		StringBuilder sb = new StringBuilder();
		sb.append("SRID=");
		sb.append(srsId);
		sb.append(";");
		sb.append("POINT(");
		sb.append(x);
		sb.append(" ");
		sb.append(y);
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

}