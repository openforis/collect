package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.idm.metamodel.Languages;
import org.zkoss.bind.annotation.Command;
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
	
	private String selectedLanguageCode;
	
	public BindingListModelListModel<String> getLanguageCodes() {
		List<String> codes = Languages.LANGUAGE_CODES;
		return new BindingListModelListModel<String>(new ListModelList<String>(codes));
	}
	
	@Command
	public void selectLanguage() {
		if ( StringUtils.isNotBlank(selectedLanguageCode) ) {
			SessionStatus sessionStatus = getSessionStatus();
			sessionStatus.setSelectedLanguageCode(selectedLanguageCode);
			Executions.sendRedirect(SURVEY_EDIT_URL);
		}
	}

	public String getSelectedLanguageCode() {
		return selectedLanguageCode;
	}

	public void setSelectedLanguageCode(String selectedLanguageCode) {
		this.selectedLanguageCode = selectedLanguageCode;
	}
	
}
