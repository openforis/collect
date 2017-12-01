package org.openforis.collect.persistence.xml;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.samplingdesign.SamplingPointGenerationSettings;
import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.model.Coordinate;

public class CeoApplicationOptions implements ApplicationOptions {

	public static final String TYPE = "ceo";
	
	private List<String> imagery = new ArrayList<String>();
	private List<Coordinate> aoiBoundary;
	private SamplingPointGenerationSettings samplingPointGenerationSettings;

	private String stackingProfile;

	private Integer imageryYear;

	private String baseMapSource;
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	public List<String> getImagery() {
		return imagery;
	}

	public void setImagery(List<String> imagery) {
		this.imagery = imagery;
	}

	public List<Coordinate> getAoiBoundary() {
		return aoiBoundary;
	}
	
	public void setAoiBoundary(List<Coordinate> aoiBoundary) {
		this.aoiBoundary = aoiBoundary;
	}
	
	public SamplingPointGenerationSettings getSamplingPointDataConfiguration() {
		return samplingPointGenerationSettings;
	}
	
	public void setSamplingPointDataConfiguration(SamplingPointGenerationSettings samplingPointGenerationSettings) {
		this.samplingPointGenerationSettings = samplingPointGenerationSettings;
	}
	
	public String getBaseMapSource() {
		return baseMapSource;
	}

	public void setBaseMapSource(String baseMapSource) {
		this.baseMapSource = baseMapSource;
	}

	public Integer getImageryYear() {
		return imageryYear;
	}
	
	public void setImageryYear(Integer imageryYear) {
		this.imageryYear = imageryYear;
	}

	public String getStackingProfile() {
		return stackingProfile;
	}
	
	public void setStackingProfile(String stackingProfile) {
		this.stackingProfile = stackingProfile;
	}
}
