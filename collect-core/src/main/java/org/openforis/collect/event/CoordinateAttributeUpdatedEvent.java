package org.openforis.collect.event;

import java.util.Date;

public class CoordinateAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private Double x;
	private Double y;
	private String srsId;
	
	public CoordinateAttributeUpdatedEvent(String surveyName, Integer recordId, int definitionId,
			Integer parentEntityId, int nodeId, Double x, Double y, String srsId, Date timestamp, String userName) {
		super(surveyName, recordId, definitionId, parentEntityId, nodeId, timestamp, userName);
		this.x = x;
		this.y = y;
		this.srsId = srsId;
	}

	public Double getX() {
		return x;
	}
	
	public Double getY() {
		return y;
	}
	
	public String getSrsId() {
		return srsId;
	}
	
}
