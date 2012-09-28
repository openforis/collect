/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.model.LanguageConfiguration;
import org.openforis.collect.persistence.SurveyImportException;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class SurveyEditVM extends SurveyEditBaseVM {

	private static final String SELECT_LANGUAGE_POP_UP_URL = "survey_edit/select_language_popup.zul";
	private static final String SRS_MANAGER_POP_UP_URL = "survey_edit/srs_popup.zul";
	private static final String SURVEYS_LIST_URL = "survey_select.zul";

	private static final String SURVEY_SUCCESSFULLY_SAVED_MESSAGE_KEY = "survey.successfully_saved";
	
	private Window selectLanguagePopUp;
	private Window srsPopUp;
	
	@Wire
	private Tree nodesTree;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
		if ( currentLanguageCode == null ) {
			//TEST
			currentLanguageCode = "eng";
			LanguageConfiguration languageConfiguration = new LanguageConfiguration();
			languageConfiguration.addLanguageCode(currentLanguageCode);
			//openLanguageManagerPopUp();
		}
	}
	
	@Command
	public void openLanguageManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			selectLanguagePopUp = openPopUp(SELECT_LANGUAGE_POP_UP_URL, true);
		}
	}
	
	@GlobalCommand
	public void openSRSManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			srsPopUp = openPopUp(SRS_MANAGER_POP_UP_URL, true);
		}
	}
	
	
	@GlobalCommand
	public void closeSRSManagerPopUp() {
		closePopUp(srsPopUp);
	}
	
	@Command
	public void backToSurveysList() {
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(null);
		sessionStatus.setCurrentLanguageCode(null);
		Executions.sendRedirect(SURVEYS_LIST_URL);
	}
	
	public void languageCodeSelected(@BindingParam("code") String selectedLanguageCode) {
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setCurrentLanguageCode(selectedLanguageCode);
		BindUtils.postGlobalCommand(null, null, SurveySelectLanguageVM.CURRENT_LANGUAGE_CHANGED_COMMAND, null);
	}
	
	@Command
	public void save() throws SurveyImportException {
		if ( checkCurrentFormValid() ) {
			surveyWorkManager.save(survey);
			MessageUtil.showInfo(SURVEY_SUCCESSFULLY_SAVED_MESSAGE_KEY);
		}
	}
	
	@GlobalCommand
	@NotifyChange({"availableLanguages"})
	public void surveyLanguagesChanged() {
		closePopUp(selectLanguagePopUp);
	}
	
	public List<String> getAvailableLanguages() {
		LanguageConfiguration langConf = survey.getLanguageConfiguration();
		if ( langConf != null ) {
	 		List<String> langCodes = langConf.getLanguageCodes();
			return new BindingListModelList<String>(langCodes, false);
		} else {
			return null;
		}
	}
	
}
