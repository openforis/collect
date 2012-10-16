package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Languages;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zkplus.databind.BindingListModelListModel;
import org.zkoss.zul.ListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyLocaleVM extends BaseVM {

	public static final String CURRENT_LANGUAGE_CHANGED_COMMAND = "currentLanguageChanged";
	public static final String SURVEY_LANGUAGES_CHANGED_COMMAND = "surveyLanguagesChanged";

	private List<String> assignableLanguageCodes;
	private List<String> assignedLanguageCodes;

	private String selectedAssignableLanguageCode;
	private String selectedCurrentLanguageCode;
	
	@Init
	public void init() {
		initAssignedLanguageCodes();
		initAssignableLanguageCodes();
		initSelectedDefaultLanguageCode();
	}
	
	protected void initAssignedLanguageCodes() {
		assignedLanguageCodes = new ArrayList<String>();
		SessionStatus sessionStatus = getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		if ( survey != null ) {
			assignedLanguageCodes.addAll(survey.getLanguages());
		}
	}
	
	protected void initAssignableLanguageCodes() {
		assignableLanguageCodes = new ArrayList<String>();
		assignableLanguageCodes.addAll(Languages.LANGUAGE_CODES);
		for (String assignedCode : assignedLanguageCodes) {
			assignableLanguageCodes.remove(assignedCode);
		}
	}
	
	protected void initSelectedDefaultLanguageCode() {
		SessionStatus sessionStatus = getSessionStatus();
		selectedCurrentLanguageCode = sessionStatus.getCurrentLanguageCode();
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
		assignedLanguageCodes.remove(selectedCurrentLanguageCode);
		assignableLanguageCodes.add(selectedCurrentLanguageCode);
		Collections.sort(assignableLanguageCodes);
	}

	@Command
	public void applyChanges() {
		SessionStatus sessionStatus = getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		List<String> oldLanguages = survey.getLanguages();
		for (String lang : oldLanguages) {
			if (! assignedLanguageCodes.contains(lang)) {
				survey.removeLanguage(lang);
			}
		}
		for (String lang : assignedLanguageCodes) {
			if ( ! oldLanguages.contains(lang) ) {
				survey.addLanguage(lang);
			}
		}
		if ( StringUtils.isNotBlank(selectedCurrentLanguageCode) ) {
			sessionStatus.setCurrentLanguageCode(selectedCurrentLanguageCode);
			BindUtils.postGlobalCommand(null, null, SURVEY_LANGUAGES_CHANGED_COMMAND, null);
			BindUtils.postGlobalCommand(null, null, CURRENT_LANGUAGE_CHANGED_COMMAND, null);
		} else {
			MessageUtil.showWarning("survey.language.error.language_not_selected");
		}
	}

	public String getSelectedAssignableLanguageCode() {
		return selectedAssignableLanguageCode;
	}

	public void setSelectedAssignableLanguageCode(
			String selectedAssignableLanguageCode) {
		this.selectedAssignableLanguageCode = selectedAssignableLanguageCode;
	}

	public String getSelectedCurrentLanguageCode() {
		return selectedCurrentLanguageCode;
	}

	public void setSelectedCurrentLanguageCode(String selectedDefaultLanguageCode) {
		this.selectedCurrentLanguageCode = selectedDefaultLanguageCode;
	}

	
}
