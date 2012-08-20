/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ModelVersion;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyViewModel {
	
	private static final String ENGLISH_LANGUAGE_CODE = "eng";

	private CollectSurvey survey;
	
	private String selectedLanguageCode;
	
	public SurveyViewModel() {
		selectedLanguageCode = ENGLISH_LANGUAGE_CODE;
		survey = new CollectSurvey();
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public String getProjectName() {
		return survey.getProjectName(selectedLanguageCode);
	}
	
	public void setProjectName(String name) {
		survey.setProjectName(selectedLanguageCode, name);
	}
	
	public String getDescription() {
		return survey.getDescription(selectedLanguageCode);
	}
	
	public void setDescription(String description) {
		survey.setDescription(selectedLanguageCode, description);
	}

	public String getSelectedLanguageCode() {
		return selectedLanguageCode;
	}

	public void setSelectedLanguageCode(String selectedLanguageCode) {
		this.selectedLanguageCode = selectedLanguageCode;
	}
	
	public ModelVersion addNewVersion() {
		int id = 0;
		List<ModelVersion> versions = survey.getVersions();
		for (ModelVersion v : versions) {
			id = Math.max(id, v.getId());
		}
		id++;
		ModelVersion version = new ModelVersion();
		version.setId(id);
		survey.addVersion(version);
		versions.add(version);
		return version;
	}
	
}
