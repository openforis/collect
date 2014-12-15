package org.openforis.collect.designer.form;

import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyMainInfoFormObject extends FormObject<CollectSurvey> {

	private String name;
	private boolean published;
	private String description;
	private String projectName;
	
	@Override
	public void loadFrom(CollectSurvey source, String languageCode) {
		name = source.getName();
		description = source.getDescription(languageCode);
		published = source.isPublished();
		projectName = source.getProjectName(languageCode);
	}
	
	@Override
	public void saveTo(CollectSurvey dest, String languageCode) {
		dest.setName(name);
		dest.setDescription(languageCode, description);
		dest.setProjectName(languageCode, projectName);
		dest.setPublished(published);
	}
	
	@Override
	protected void reset() {
		// TODO Auto-generated method stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPublished() {
		return published;
	}
	
	public void setPublished(boolean published) {
		this.published = published;
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

}
