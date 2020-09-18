package org.openforis.collect.event;

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

	public String getSrsId() {
		return srsId;
	}

	public void setSrsId(String srsId) {
		this.srsId = srsId;
	}
}
