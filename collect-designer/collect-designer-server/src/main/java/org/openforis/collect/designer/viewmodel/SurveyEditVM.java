/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zkplus.databind.BindingListModelListModel;
import org.zkoss.zul.ListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyEditVM {
	
	private static final String ENGLISH_LANGUAGE_CODE = "eng";

//	@WireVariable
//	private SurveyManager surveyManager;
	
	private CollectSurvey survey;
	
	private String selectedLanguageCode;
	
	private Map<ModelVersion, Boolean> versionsEditingStatus;
	
	public SurveyEditVM() {
		selectedLanguageCode = ENGLISH_LANGUAGE_CODE;
		survey = new CollectSurvey();
		versionsEditingStatus = new HashMap<ModelVersion, Boolean>();
	}
	
	public BindingListModelListModel<String> getLanguageCodes() {
		return new BindingListModelListModel<String>(new ListModelList<String>(Languages.LANGUAGE_CODES));
	}
	
	@NotifyChange("versions")
	@Command
	public void newVersion() {
		int id = 0;
		List<ModelVersion> versions = survey.getVersions();
		for (ModelVersion v : versions) {
			id = Math.max(id, v.getId());
		}
		id++;
		ModelVersion version = new ModelVersion();
		version.setId(id);
		survey.addVersion(version);
		changeVersionEditableStatus(version);
	}
	
	public List<ModelVersion> getVersions() {
		return survey.getVersions();
	}
	
	@Command
	public void changeVersionEditableStatus(@BindingParam("currentVersion") ModelVersion version) {
		Boolean editable = versionsEditingStatus.get(version);
		versionsEditingStatus.put(version, editable == null ? Boolean.TRUE : new Boolean(! editable.booleanValue()));
		refreshRowTemplate(version);
	}

	@Command
	public void confirmVersionEdit(@BindingParam("version") ModelVersion version) {
		changeVersionEditableStatus(version);
	}

	public void refreshRowTemplate(ModelVersion version) {
		/*
		 * This code is special and notifies ZK that the bean's value
		 * has changed as it is used in the template mechanism.
		 * This stops the entire Grid's data from being refreshed
		 */
		BindUtils.postNotifyChange(null, null, this, "versionsEditingStatus");
	}

	public Map<ModelVersion, Boolean> getVersionsEditingStatus() {
		return versionsEditingStatus;
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

	
}
