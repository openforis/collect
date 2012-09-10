package org.openforis.collect.designer.form;

import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyMainInfoFormObject extends ItemFormObject<CollectSurvey> {

	private String name;
	private String uri;
	private String description;
	private String projectName;
	private Integer projectCycle;
	
	@Override
	public void loadFrom(CollectSurvey source, String languageCode) {
		name = source.getName();
		description = source.getDescription(languageCode);
		uri = source.getUri();
		projectName = source.getProjectName(languageCode);
		projectCycle = source.getCycle();
	}
	
	@Override
	public void saveTo(CollectSurvey dest, String languageCode) {
		dest.setName(name);
		dest.setUri(uri);
		dest.setDescription(languageCode, description);
		dest.setProjectName(languageCode, projectName);
		dest.setCycle(projectCycle);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Integer getProjectCycle() {
		return projectCycle;
	}

	public void setProjectCycle(Integer projectCycle) {
		this.projectCycle = projectCycle;
	}
	
}
