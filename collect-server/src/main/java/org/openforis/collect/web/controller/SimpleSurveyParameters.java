package org.openforis.collect.web.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.io.metadata.samplingpointdata.SamplingPointDataGenerator.PointsConfiguration;

public class SimpleSurveyParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private Double boundaryLonMin;
	private Double boundaryLonMax;
	private Double boundaryLatMin;
	private Double boundaryLatMax; 
	private List<PointsConfiguration> samplingPointsConfigurationByLevels = new ArrayList<PointsConfiguration>(3);
	private List<Object> sampleValues = new ArrayList<Object>();
	private List<String> imagery = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Double getBoundaryLonMin() {
		return boundaryLonMin;
	}
	
	public void setBoundaryLonMin(Double boundaryLonMin) {
		this.boundaryLonMin = boundaryLonMin;
	}
	
	public Double getBoundaryLonMax() {
		return boundaryLonMax;
	}
	
	public void setBoundaryLonMax(Double boundaryLonMax) {
		this.boundaryLonMax = boundaryLonMax;
	}
	
	public Double getBoundaryLatMin() {
		return boundaryLatMin;
	}
	
	public void setBoundaryLatMin(Double boundaryLatMin) {
		this.boundaryLatMin = boundaryLatMin;
	}
	
	public Double getBoundaryLatMax() {
		return boundaryLatMax;
	}
	
	public void setBoundaryLatMax(Double boundaryLatMax) {
		this.boundaryLatMax = boundaryLatMax;
	}
	
	public List<Object> getSampleValues() {
		return sampleValues;
	}

	public void setSampleValues(List<Object> sampleValues) {
		this.sampleValues = sampleValues;
	}

	public List<String> getImagery() {
		return imagery;
	}

	public void setImagery(List<String> imagery) {
		this.imagery = imagery;
	}

	public List<PointsConfiguration> getSamplingPointsConfigurationByLevels() {
		return samplingPointsConfigurationByLevels;
	}

	public void setSamplingPointsConfigurationByLevels(List<PointsConfiguration> samplingPointsConfigurationByLevels) {
		this.samplingPointsConfigurationByLevels = samplingPointsConfigurationByLevels;
	}
	
}