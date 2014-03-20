package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.model.LabelledItem;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.manager.SurveyObjectsGenerator;
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

	private List<LabelledItem> languages;
	private BindingListModelListModel<LabelledItem> languagesModel;
	
	@Init
	public void init() {
		languages = new ArrayList<LabelledItem>();
		List<String> codes = Languages.getCodes(Standard.ISO_639_1);
		for (String code : codes) {
			LabelledItem item = new LabelledItem(code, Labels.getLabel(code));
			languages.add(item);
		}
		Collections.sort(languages, new LabelComparator());
		languagesModel = new BindingListModelListModel<LabelledItem>(new ListModelList<LabelledItem>(languages));
		languagesModel.setMultiple(true);
		List<LabelledItem> assignedLanguages = getAssignedLanguages();
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
	
	protected List<LabelledItem> getAssignedLanguages() {
		List<LabelledItem> result = new ArrayList<LabelledItem>();
		List<String> assignedLanguageCodes = getAssignedLanguageCodes();
		for (String code : assignedLanguageCodes) {
			for (LabelledItem item : languages) {
				if ( item.getCode().equals(code)) { 
					result.add(item);
				}
			}
		}
		return result;
	}
	
	public BindingListModelListModel<LabelledItem> getLanguagesModel() {
		return languagesModel;
	}
	
	public List<String> getSelectedLanguageCodes() {
		List<String> result = new ArrayList<String>();
		Set<LabelledItem> languages = languagesModel.getSelection();
		if ( languages != null ) {
			for (LabelledItem item : languages) {
				result.add(item.getCode());
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
		boolean firstTime = oldLangCodes.isEmpty();
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
		if ( firstTime ) {
			SurveyObjectsGenerator surveyObjectsGenereator = new SurveyObjectsGenerator();
			surveyObjectsGenereator.addPredefinedObjects(survey);
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

	private class LabelComparator implements Comparator<LabelledItem> {

		@Override
		public int compare(LabelledItem item1, LabelledItem item2) {
			return item1.getLabel().compareTo(item2.getLabel());
		}
		
	}
	
}
