/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.converter.XMLStringDateConverter;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelListModel;
import org.zkoss.zul.ListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyEditVM {
	
	private static final String ENGLISH_LANGUAGE_CODE = "eng";

	protected XMLStringDateConverter xmlStringDateConverter = new XMLStringDateConverter();
	
	private String dateFormat = "dd/MM/yyyy";
	
	@WireVariable
	protected SurveyManager surveyManager;
	
	@WireVariable
	protected CollectSurvey survey;
	
	protected String selectedLanguageCode;
	
	public SurveyEditVM() {
		selectedLanguageCode = ENGLISH_LANGUAGE_CODE;
		survey = new CollectSurvey();
	}
	
	@Command
	public void save() throws SurveyImportException {
		surveyManager.updateModel(survey);
	}
	
	public BindingListModelListModel<String> getLanguageCodes() {
		return new BindingListModelListModel<String>(new ListModelList<String>(Languages.LANGUAGE_CODES));
	}
	
	public CollectSurvey getSurvey() {
		return survey;
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

	public XMLStringDateConverter getXmlStringDateConverter() {
		return xmlStringDateConverter;
	}

	public String getDateFormat() {
		return dateFormat;
	}
	
}
