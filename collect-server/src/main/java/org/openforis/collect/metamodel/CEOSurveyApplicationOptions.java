package org.openforis.collect.metamodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.web.controller.SingleAttributeSurveyCreationParameters.SamplingPointDataConfiguration;
import org.openforis.idm.metamodel.ApplicationOptions;

public class CEOSurveyApplicationOptions implements ApplicationOptions {

	private List<String> imagery = new ArrayList<String>();
	private SamplingPointDataConfiguration samplingPointDataConfiguration;
	
	@Override
	public String getType() {
		return "CEO";
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
}
