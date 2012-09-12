/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.converter.XMLStringDateConverter;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.LanguageConfiguration;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyEditBaseVM extends BaseVM {
	
	protected XMLStringDateConverter xmlStringDateConverter = new XMLStringDateConverter();
	
	private String dateFormat = "dd/MM/yyyy";
	
	@WireVariable
	protected SurveyManager surveyManager;
	
	@WireVariable
	protected CollectSurvey survey;

	protected String currentLanguageCode;
	
	protected boolean currentFormValid;
	
	public SurveyEditBaseVM() {
		currentFormValid = true;
		initCurrentLanguageCode();
	}

	private void initCurrentLanguageCode() {
		SessionStatus sessionStatus = getSessionStatus();
		currentLanguageCode = sessionStatus.getCurrentLanguageCode();
	}
	
	@Init
	public void init() {
		initSurvey();
	}
	
	@Command
	public void save() throws SurveyImportException {
		surveyManager.updateModel(survey);
	}
	
	@GlobalCommand
	@NotifyChange("versionsForCombo")
	public void versionsUpdated() {}

	@GlobalCommand
	@NotifyChange("codeLists")
	public void codeListsUpdated() {}
	
	@GlobalCommand
	@NotifyChange("units")
	public void unitsUpdated() {}
	
	@GlobalCommand
	@NotifyChange("tabDefinitions")
	public void tabDefinitionsUpdated() {}
	
	@GlobalCommand
	public void currentFormValidated(@BindingParam("valid") boolean valid) {
		currentFormValid = valid;
	}

	protected void initSurvey() {
		if ( survey == null ) {
			SessionStatus sessionStatus = getSessionStatus();
			survey = sessionStatus.getSurvey();
		}
	}

	public CollectSurvey getSurvey() {
		if ( survey == null ) {
			initSurvey();
		}
		return survey;
	}

	@GlobalCommand
	@NotifyChange({"currentLanguageCode"})
	public void currentLanguageChanged() {
		initCurrentLanguageCode();
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public List<ModelVersion> getVersionsForCombo() {
		CollectSurvey survey = getSurvey();
		List<ModelVersion> result = new ArrayList<ModelVersion>(survey.getVersions());
		result.add(0, FormObject.VERSION_EMPTY_SELECTION);
		return new BindingListModelList<ModelVersion>(result, false);
	}
	
	public List<CodeList> getCodeLists() {
		CollectSurvey survey = getSurvey();
		List<CodeList> result = new ArrayList<CodeList>(survey.getCodeLists());
		return new BindingListModelList<CodeList>(result, false);
	}
	
	public List<Unit> getUnits() {
		List<Unit> result = new ArrayList<Unit>(survey.getUnits());
		return new BindingListModelList<Unit>(result, false);
	}

	public String getCurrentLanguageCode() {
		return currentLanguageCode;
	}
	
	public boolean isCurrentFormValid() {
		return currentFormValid;
	}

	public void setCurrentFormValid(boolean currentFormValid) {
		this.currentFormValid = currentFormValid;
	}
	
}
