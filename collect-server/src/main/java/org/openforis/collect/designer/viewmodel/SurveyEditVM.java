/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class SurveyEditVM extends SurveyBaseVM {

	private static final String SURVEY_SUCCESSFULLY_SAVED_MESSAGE_KEY = "survey.successfully_saved";
//	private static final String SURVEY_SUCCESSFULLY_PUBLISHED_MESSAGE_KEY = "survey.successfully_published";
	
	private Window selectLanguagePopUp;
	private Window srsPopUp;
	
	@WireVariable
	private SurveyManager surveyManager;
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
		if ( survey == null ) {
			backToSurveysList();
		} else {
			currentLanguageCode = survey.getDefaultLanguage();
			if ( currentLanguageCode == null ) {
				openLanguageManagerPopUp();
			}
//			TEST
//			currentLanguageCode = "eng";
//			uiConf.addLanguageCode("eng");
//			uiConf.addLanguageCode("spa");
//			notifyChange("availableLanguages");
		}
	}
	
	@Command
	public void openLanguageManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			selectLanguagePopUp = openPopUp(Resources.Component.SELECT_LANGUAGE_POP_UP.getLocation(), true);
		}
	}
	
	@GlobalCommand
	public void openSRSManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			srsPopUp = openPopUp(Resources.Component.SRS_MANAGER_POP_UP.getLocation(), true);
		}
	}
	
	@GlobalCommand
	public void closeSRSManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			closePopUp(srsPopUp);
			srsPopUp = null;
		}
	}
	
	@Command
	public void backToSurveysList() {
		resetSessionStatus();
		showMainPage();
	}

	protected void showMainPage() {
		Executions.sendRedirect(Resources.Page.MAIN.getLocation());
	}

	protected void resetSessionStatus() {
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.reset();
	}
	
	@Command
	@NotifyChange({"currentLanguageCode"})
	public void languageCodeSelected(@BindingParam("code") String selectedLanguageCode) {
		SessionStatus sessionStatus = getSessionStatus();
		if ( checkCurrentFormValid() ) {
			sessionStatus.setCurrentLanguageCode(selectedLanguageCode);
			BindUtils.postGlobalCommand(null, null, SurveyLocaleVM.CURRENT_LANGUAGE_CHANGED_COMMAND, null);
		}
		currentLanguageCode = sessionStatus.getCurrentLanguageCode();
	}
	
	@Command
	public void save() throws SurveyImportException {
		if ( checkCurrentFormValid() ) {
			surveyManager.saveSurveyWork(survey);
			MessageUtil.showInfo(SURVEY_SUCCESSFULLY_SAVED_MESSAGE_KEY);
			BindUtils.postNotifyChange(null, null, survey, "published");
		}
	}
	
	@Command
	public void publish() {
		if ( checkCurrentFormValid() ) {
			MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performSurveyPublishing();
				}
			}, "survey.publish.confirm");
		}
	}

	protected void performSurveyPublishing() {
		try {
			surveyManager.publish(survey);
			backToSurveysList();
		} catch (SurveyImportException e) {
			throw new RuntimeException(e);
		}
	}
	
	@GlobalCommand
	@NotifyChange({"availableLanguages"})
	public void surveyLanguagesChanged() {
		closePopUp(selectLanguagePopUp);
		selectLanguagePopUp = null;
	}
	
	public List<String> getAvailableLanguages() {
		CollectSurvey survey = getSurvey();
		if ( survey == null ) {
			//TODO session expired?
			return null;
		} else {
			List<String> languages = survey.getLanguages();
			return new BindingListModelList<String>(languages, false);
		}
	}
	
}
