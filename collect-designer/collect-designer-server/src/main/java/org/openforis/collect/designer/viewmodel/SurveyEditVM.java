/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.converter.XMLStringDateConverter;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelList;
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
	}
	
	@Command
	public void save() throws SurveyImportException {
		surveyManager.updateModel(survey);
	}
	
	@NotifyChange("versionsForCombo")
	@GlobalCommand
	public void versionsUpdated() {}

	@NotifyChange("codeLists")
	@GlobalCommand
	public void codeListsUpdated() {}
	
	@NotifyChange("units")
	@GlobalCommand
	public void unitsUpdated() {}
	
	@NotifyChange("tabDefinitions")
	@GlobalCommand
	public void tabDefinitionsUpdated() {}
	
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

	public List<ModelVersion> getVersionsForCombo() {
		List<ModelVersion> result = new ArrayList<ModelVersion>(survey.getVersions());
		result.add(0, FormObject.VERSION_EMPTY_SELECTION);
		return new BindingListModelList<ModelVersion>(result, false);
	}
	
	public List<CodeList> getCodeLists() {
		List<CodeList> result = new ArrayList<CodeList>(survey.getCodeLists());
		return new BindingListModelList<CodeList>(result, false);
	}
	
	public List<Unit> getUnits() {
		List<Unit> result = new ArrayList<Unit>(survey.getUnits());
		return new BindingListModelList<Unit>(result, false);
	}
	
}
