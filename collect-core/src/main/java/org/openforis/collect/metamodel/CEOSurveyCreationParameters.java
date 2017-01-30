package org.openforis.collect.metamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.samplingdesign.SamplingPointGenerationSettings;

/**
 * 
 * @author S. Ricci
 *
 */
public class CEOSurveyCreationParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private List<ListItem> values = new ArrayList<ListItem>();
	private List<String> imagery = new ArrayList<String>();
	private SamplingPointGenerationSettings samplingPointGenerationSettings;
	
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
	
	public List<ListItem> getValues() {
		return values;
	}
	
	public void setValues(List<ListItem> values) {
		this.values = values;
	}

	public List<String> getImagery() {
		return imagery;
	}

	public void setImagery(List<String> imagery) {
		this.imagery = imagery;
	}
	
	public SamplingPointGenerationSettings getSamplingPointGenerationSettings() {
		return samplingPointGenerationSettings;
	}
	
	public void setSamplingPointGenerationSettings(SamplingPointGenerationSettings samplingPointGenerationSettings) {
		this.samplingPointGenerationSettings = samplingPointGenerationSettings;
	}
	
	public static class ListItem {
		
		private String code;
		private String label;
		private String color;

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}
	}
	
}