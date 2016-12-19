package org.openforis.collect.web.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.io.metadata.samplingpointdata.SamplingPointDataGenerator.PointsConfiguration;

public class SingleAttributeSurveyCreationParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private List<Object> values = new ArrayList<Object>();
	private List<String> imagery = new ArrayList<String>();
	private SamplingPointDataConfiguration samplingPointDataConfiguration;
	
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
	
	public List<Object> getValues() {
		return values;
	}
	
	public void setValues(List<Object> values) {
		this.values = values;
	}

	public List<String> getImagery() {
		return imagery;
	}

	public void setImagery(List<String> imagery) {
		this.imagery = imagery;
	}
	
	public SamplingPointDataConfiguration getSamplingPointDataConfiguration() {
		return samplingPointDataConfiguration;
	}
	
	public void setSamplingPointDataConfiguration(SamplingPointDataConfiguration samplingPointDataConfiguration) {
		this.samplingPointDataConfiguration = samplingPointDataConfiguration;
	}

	public static class SamplingPointDataConfiguration {
		
		private Double boundaryLonMin;
		private Double boundaryLonMax;
		private Double boundaryLatMin;
		private Double boundaryLatMax; 
		private List<PointsConfiguration> levelsConfiguration = new ArrayList<PointsConfiguration>(3);
		
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
		
		public List<PointsConfiguration> getLevelsConfiguration() {
			return levelsConfiguration;
		}
		
		public void setLevelsConfiguration(List<PointsConfiguration> levelsConfiguration) {
			this.levelsConfiguration = levelsConfiguration;
		}
		
	}
	
}