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
}
