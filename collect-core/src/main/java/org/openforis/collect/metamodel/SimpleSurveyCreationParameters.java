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
public class SimpleSurveyCreationParameters implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String projectName;
	private String description;
	private List<SimpleCodeList> codeLists = new ArrayList<SimpleCodeList>();
	private List<String> imagery = new ArrayList<String>();
	private SamplingPointGenerationSettings samplingPointGenerationSettings;
	private int userGroupId;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<SimpleCodeList> getCodeLists() {
		return codeLists;
	}
	
	public void setCodeLists(List<SimpleCodeList> codeLists) {
		this.codeLists = codeLists;
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
	
	public int getUserGroupId() {
		return userGroupId;
	}
	
	public void setUserGroupId(int userGroupId) {
		this.userGroupId = userGroupId;
	}
	
	public static class SimpleCodeList {
		
		private String name;
		private List<ListItem> items;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public List<ListItem> getItems() {
			return items;
		}
		
		public void setItems(List<ListItem> items) {
			this.items = items;
		}
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