package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
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

	private List<LanguageItem> languages;
	private BindingListModelListModel<LanguageItem> languagesModel;
	
	@Init
	public void init() {
		languages = new ArrayList<LanguageItem>();
		List<String> codes = Languages.getCodes(Standard.ISO_639_1);
		for (String code : codes) {
			LanguageItem item = new LanguageItem(code, Labels.getLabel(code));
			languages.add(item);
		}
		Collections.sort(languages, new LanguageComparator());
		languagesModel = new BindingListModelListModel<LanguageItem>(new ListModelList<LanguageItem>(languages));
		languagesModel.setMultiple(true);
		List<LanguageItem> assignedLanguages = getAssignedLanguages();
		languagesModel.setSelection(assignedLanguages);
	}
	
	protected List<String> getAssignedLanguageCodes() {
		SessionStatus sessionStatus = getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		if ( survey == null ) {
			return null;
		} else {
			return survey.getLanguages();
		}
	}
	
	protected List<LanguageItem> getAssignedLanguages() {
		List<LanguageItem> result = new ArrayList<LanguageItem>();
		List<String> assignedLanguageCodes = getAssignedLanguageCodes();
		for (String code : assignedLanguageCodes) {
			for (LanguageItem item : languages) {
				if ( item.code.equals(code)) { 
					result.add(item);
				}
			}
		}
		return result;
	}
	
	public BindingListModelListModel<LanguageItem> getLanguagesModel() {
		return languagesModel;
	}
	
	public List<String> getSelectedLanguageCodes() {
		List<String> result = new ArrayList<String>();
		Set<LanguageItem> languages = languagesModel.getSelection();
		if ( languages != null ) {
			for (LanguageItem item : languages) {
				result.add(item.code);
			}
		}
		//TODO sort...
		return result;
	}
	
	@Command
	public void applyChanges() {
		SessionStatus sessionStatus = getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		List<String> selectedLanguageCodes = getSelectedLanguageCodes();
		List<String> oldLangCodes = new ArrayList<String>(survey.getLanguages());
		for (String oldLangCode : oldLangCodes) {
			if (! selectedLanguageCodes.contains(oldLangCode)) {
				survey.removeLanguage(oldLangCode);
			}
		}
		for (String lang : selectedLanguageCodes) {
			if ( ! oldLangCodes.contains(lang) ) {
				survey.addLanguage(lang);
			}
		}
		String selectedCurrentLanguageCode = selectedLanguageCodes.isEmpty() ? null: selectedLanguageCodes.iterator().next();
		if ( StringUtils.isNotBlank(selectedCurrentLanguageCode) ) {
			sessionStatus.setCurrentLanguageCode(selectedCurrentLanguageCode);
			BindUtils.postGlobalCommand(null, null, SURVEY_LANGUAGES_CHANGED_COMMAND, null);
			BindUtils.postGlobalCommand(null, null, CURRENT_LANGUAGE_CHANGED_COMMAND, null);
		} else {
			MessageUtil.showWarning("survey.language.error.select_at_least_one_language");
		}
	}

	public static class LanguageItem {
		private String code;
		private String label;

		public LanguageItem(String code, String label) {
			this.code = code;
			this.label = label;
		}

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
		
	}
	
	private class LanguageComparator implements Comparator<LanguageItem> {

		@Override
		public int compare(LanguageItem item1, LanguageItem item2) {
			return item1.label.compareTo(item2.label);
		}
		
	}
	
}
