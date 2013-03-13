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
	private String uri;
	private String description;
	private String projectName;
	private String projectCycle;
	
	@Override
	public void loadFrom(CollectSurvey source, String languageCode, String defaultLanguage) {
		name = source.getName();
		description = getDescription(source, languageCode, defaultLanguage);
		uri = source.getUri();
		published = source.isPublished();
		projectName = getProjectName(source, languageCode, defaultLanguage);
		projectCycle = source.getCycle();
	}
	
	@Override
	public void saveTo(CollectSurvey dest, String languageCode) {
		dest.setName(name);
		dest.setUri(uri);
		dest.setDescription(languageCode, description);
		dest.setProjectName(languageCode, projectName);
		dest.setCycle(projectCycle);
		dest.setPublished(published);
	}
	
	@Override
	protected void reset() {
		// TODO Auto-generated method stub
	}

	protected String getDescription(CollectSurvey source, String languageCode, String defaultLanguage) {
		String result = source.getDescription(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getDescription(null);
		}
		return result;
	}

	protected String getProjectName(CollectSurvey source, String languageCode, String defaultLanguage) {
		String result = source.getProjectName(languageCode);
		if ( result == null && languageCode != null && languageCode.equals(defaultLanguage) ) {
			//try to get the label associated to default language
			result = source.getDescription(null);
		}
		return result;
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

	public String getProjectCycle() {
		return projectCycle;
	}

	public void setProjectCycle(String projectCycle) {
		this.projectCycle = projectCycle;
	}
	
}
