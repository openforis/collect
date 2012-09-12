package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.LanguageConfiguration;
import org.openforis.idm.metamodel.Languages;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zkplus.databind.BindingListModelListModel;
import org.zkoss.zul.ListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveySelectLanguageVM extends BaseVM {

	private static final String SURVEY_EDIT_URL = "survey_edit.zul";
	
	private String selectedAssignableLanguageCode;
	private String selectedDefaultLanguageCode;
	private List<String> assignableLanguageCodes;
	private List<String> assignedLanguageCodes;
	
	public SurveySelectLanguageVM() {
		initAssignedLanguageCodes();
		initAssignableLanguageCodes();
	}
	
	protected void initAssignedLanguageCodes() {
		assignedLanguageCodes = new ArrayList<String>();
		SessionStatus sessionStatus = getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		if ( survey != null ) {
			LanguageConfiguration languageConfiguration = survey.getLanguageConfiguration();
			if ( languageConfiguration != null ) {
				assignedLanguageCodes.addAll(languageConfiguration.getLanguageCodes());
			}
		}
	}
	
	protected void initAssignableLanguageCodes() {
		assignableLanguageCodes = new ArrayList<String>();
		assignableLanguageCodes.addAll(Languages.LANGUAGE_CODES);
		for (String assignedCode : assignedLanguageCodes) {
			assignableLanguageCodes.remove(assignedCode);
		}
	}
	
	public BindingListModelListModel<String> getAssignableLanguageCodes() {
		return new BindingListModelListModel<String>(new ListModelList<String>(assignableLanguageCodes));
	}
	
	public BindingListModelListModel<String> getAssignedLanguageCodes() {
		return new BindingListModelListModel<String>(new ListModelList<String>(assignedLanguageCodes));
	}
	
	@Command
	@NotifyChange({"assignedLanguageCodes","assignableLanguageCodes"})
	public void assignLanguage() {
		assignedLanguageCodes.add(selectedAssignableLanguageCode);
		assignableLanguageCodes.remove(selectedAssignableLanguageCode);
	}
	
	@Command
	@NotifyChange({"assignedLanguageCodes","assignableLanguageCodes"})
	public void removeLanguage() {
		assignedLanguageCodes.remove(selectedDefaultLanguageCode);
		assignableLanguageCodes.add(selectedDefaultLanguageCode);
		Collections.sort(assignableLanguageCodes);
	}

	@Command
	public void applyChanges() {
		SessionStatus sessionStatus = getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		LanguageConfiguration langConf = new LanguageConfiguration();
		langConf.addLanguageCodes(assignedLanguageCodes);
		survey.setLanguageConfiguration(langConf);
		if ( StringUtils.isNotBlank(selectedDefaultLanguageCode) ) {
			sessionStatus.setSelectedLanguageCode(selectedDefaultLanguageCode);
			Executions.sendRedirect(SURVEY_EDIT_URL);
		}
	}

	public String getSelectedAssignableLanguageCode() {
		return selectedAssignableLanguageCode;
	}

	public void setSelectedAssignableLanguageCode(
			String selectedAssignableLanguageCode) {
		this.selectedAssignableLanguageCode = selectedAssignableLanguageCode;
	}

	public String getSelectedDefaultLanguageCode() {
		return selectedDefaultLanguageCode;
	}

	public void setSelectedDefaultLanguageCode(String selectedDefaultLanguageCode) {
		this.selectedDefaultLanguageCode = selectedDefaultLanguageCode;
	}

	
}
