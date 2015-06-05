package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.designer.model.LabelledItem;
import org.openforis.collect.designer.model.LabelledItem.LabelComparator;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyLanguageVM extends BaseVM {

	public static final String CURRENT_LANGUAGE_CHANGED_COMMAND = "currentLanguageChanged";
	public static final String SURVEY_LANGUAGES_CHANGED_COMMAND = "surveyLanguagesChanged";
	public static final String CLOSE_SURVEY_LANGUAGE_SELECT_POPUP_COMMAND = "closeSurveyLanguageSelectPopUp";
	private static final int MAX_LANGUAGES = 3;
	
	private List<LabelledItem> languages;
	private List<LabelledItem> assignedLanguages;
	private LabelledItem selectedAssignedLanguage;
	private LabelledItem selectedLanguageToAssign;
	
	@Init
	public void init() {
		languages = new ArrayList<LabelledItem>();
		List<String> codes = Languages.getCodes(Standard.ISO_639_1);
		for (String code : codes) {
			LabelledItem item = new LabelledItem(code, Labels.getLabel(code));
			languages.add(item);
		}
		Collections.sort(languages, new LabelComparator());
		assignedLanguages = getSurveyAssignedLanguages();
	}
	
	protected List<String> getSurveyAssignedLanguageCodes() {
		SessionStatus sessionStatus = getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		if ( survey == null ) {
			return null;
		} else {
			return survey.getLanguages();
		}
	}
	
	protected List<LabelledItem> getSurveyAssignedLanguages() {
		List<LabelledItem> result = new ArrayList<LabelledItem>();
		List<String> assignedLanguageCodes = getSurveyAssignedLanguageCodes();
		for (String code : assignedLanguageCodes) {
			for (LabelledItem item : languages) {
				if ( item.getCode().equals(code)) { 
					result.add(item);
				}
			}
		}
		return result;
	}
	
	public List<String> getSelectedLanguageCodes() {
		List<String> result = new ArrayList<String>();
		for (LabelledItem item : assignedLanguages) {
			result.add(item.getCode());
		}
		return result;
	}
	
	public List<LabelledItem> getLanguages() {
		return languages;
	}
	
	@Command
	@NotifyChange({"assignedLanguages", "selectedLanguageToAssign"})
	public void addLanguage() {
		if (assignedLanguages.size() == MAX_LANGUAGES) {
			MessageUtil.showWarning("survey.language.error.maximum_number_of_languages_reached", new Object[]{MAX_LANGUAGES});
			return;
		}
		assignedLanguages.add(selectedLanguageToAssign);
		selectedLanguageToAssign = null;
	}
	
	@Command
	@NotifyChange({"assignedLanguages", "selectedAssignedLanguage"})
	public void removeLanguage() {
		String defaultLangCode = getSurveyAssignedLanguageCodes().get(0);
		if (selectedAssignedLanguage.getCode().equals(defaultLangCode)) {
			MessageUtil.showWarning("survey.language.error.cannot_remove_default_language");
			return;
		}
		assignedLanguages.remove(selectedAssignedLanguage);
		selectedAssignedLanguage = null;
	}
	
	@Command
	public void applyChanges() {
		SessionStatus sessionStatus = getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		
		List<String> selectedLanguageCodes = getSelectedLanguageCodes();
		List<String> oldLangCodes = new ArrayList<String>(survey.getLanguages());
		// remove languages from survey
		for (String oldLangCode : oldLangCodes) {
			if (! selectedLanguageCodes.contains(oldLangCode)) {
				survey.removeLanguage(oldLangCode);
			}
		}
		// add new languages
		for (String lang : selectedLanguageCodes) {
			if ( ! oldLangCodes.contains(lang) ) {
				survey.addLanguage(lang);
			}
		}
		// sort languages
		for (int i = 0; i < selectedLanguageCodes.size(); i++) {
			String lang = selectedLanguageCodes.get(i);
			survey.moveLanguage(lang, i);
		}

		if ( assignedLanguages.isEmpty() ) {
			MessageUtil.showWarning("survey.language.error.select_at_least_one_language");
		} else {
			sessionStatus.setCurrentLanguageCode(survey.getDefaultLanguage());
			BindUtils.postGlobalCommand(null, null, SURVEY_LANGUAGES_CHANGED_COMMAND, null);
			BindUtils.postGlobalCommand(null, null, CURRENT_LANGUAGE_CHANGED_COMMAND, null);
		}
	}
	
	@Command
	public void close() {
		BindUtils.postGlobalCommand(null, null, CLOSE_SURVEY_LANGUAGE_SELECT_POPUP_COMMAND, null);
	}
	
	public List<LabelledItem> getAssignedLanguages() {
		return assignedLanguages;
	}
	
	@DependsOn("assignedLanguages")
	public List<LabelledItem> getUnassignedLanguages() {
		@SuppressWarnings("unchecked")
		List<LabelledItem> result = new ArrayList<LabelledItem>(org.apache.commons.collections.CollectionUtils.disjunction(languages, assignedLanguages));
		Collections.sort(result, new LabelledItem.LabelComparator());
		return result;
	}
	
	@Command
	@NotifyChange({"assignedLanguages"})
	public void moveSelectedAssignedLanguageUp() {
		moveSelectedAssignedLanguage(true);
	}
	
	@Command
	@NotifyChange({"assignedLanguages"})
	public void moveSelectedAssignedLanguageDown() {
		moveSelectedAssignedLanguage(false);
	}
	
	protected void moveSelectedAssignedLanguage(boolean up) {
		int indexFrom = getSelectedAssignedLanguageIndex();
		int indexTo = up ? indexFrom - 1: indexFrom + 1;
		moveSelectedAssignedLanguage(indexTo);
	}
	
	protected int getSelectedAssignedLanguageIndex() {
		int index = assignedLanguages.indexOf(selectedAssignedLanguage);
		return index;
	}

	protected void moveSelectedAssignedLanguage(int indexTo) {
		CollectionUtils.shiftItem(assignedLanguages, selectedAssignedLanguage, indexTo);
	}
	
	@DependsOn({"assignedLanguages","selectedAssignedLanguage"})
	public boolean isMoveSelectedAssignedLanguageUpDisabled() {
		return isMoveSelectedAssignedLanguageDisabled(true);
	}
	
	@DependsOn({"assignedLanguages","selectedAssignedLanguage"})
	public boolean isMoveSelectedAssignedLanguageDownDisabled() {
		return isMoveSelectedAssignedLanguageDisabled(false);
	}
	
	protected boolean isMoveSelectedAssignedLanguageDisabled(boolean up) {
		if ( selectedAssignedLanguage == null ) {
			return true;
		} else {
			List<LabelledItem> siblings = assignedLanguages;
			int index = siblings.indexOf(selectedAssignedLanguage);
			return up ? index <= 0: index < 0 || index >= siblings.size() - 1;
		}
	}

	public LabelledItem getSelectedAssignedLanguage() {
		return selectedAssignedLanguage;
	}
	
	public void setSelectedAssignedLanguage(
			LabelledItem selectedAssignedLanguage) {
		this.selectedAssignedLanguage = selectedAssignedLanguage;
	}
	
	public LabelledItem getSelectedLanguageToAssign() {
		return selectedLanguageToAssign;
	}
	
	public void setSelectedLanguageToAssign(
			LabelledItem selectedLanguageToAssign) {
		this.selectedLanguageToAssign = selectedLanguageToAssign;
	}
	
}
