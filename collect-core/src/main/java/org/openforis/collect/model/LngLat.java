package org.openforis.collect.model;

public class LngLat {

	private Long longitude;
	private Long latitude;

	public LngLat(Long longitude, Long latitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public Long getLongitude() {
		return longitude;
	}

	public void setLongitude(Long longitude) {
		this.longitude = longitude;
	}

	public Long getLatitude() {
		return latitude;
	}

	public void setLatitude(Long latitude) {
		this.latitude = latitude;
	}	
}
