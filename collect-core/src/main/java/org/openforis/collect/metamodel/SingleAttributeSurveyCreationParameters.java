package org.openforis.collect.metamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.samplingdesign.SamplingPointGenerationSettings;

public class SingleAttributeSurveyCreationParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private List<Object> values = new ArrayList<Object>();
	private List<String> imagery = new ArrayList<String>();
	private SamplingPointGenerationSettings samplingPointConfiguration;
	
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
	
	public SamplingPointGenerationSettings getSamplingPointGenerationSettings() {
		return samplingPointConfiguration;
	}
	
	public void setSamplingPointGenerationSettings(SamplingPointGenerationSettings samplingPointGenerationSettings) {
		this.samplingPointConfiguration = samplingPointGenerationSettings;
	}
	
}