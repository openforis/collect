package org.openforis.collect.event;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class CoordinateAttributeUpdatedEvent extends AttributeUpdatedEvent {

	private Double x;
	private Double y;
	private String srsId;

	public CoordinateAttributeUpdatedEvent(String surveyName, Integer recordId,
			RecordStep step, String definitionId, List<String> ancestorIds,
			String nodeId, Double x, Double y, String srsId, Date timestamp,
			String userName) {
		super(surveyName, recordId, step, definitionId, ancestorIds, nodeId,
				timestamp, userName);
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
