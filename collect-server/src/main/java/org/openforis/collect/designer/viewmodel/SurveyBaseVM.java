/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.model.LabelKeys.EMPTY_OPTION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.converter.XMLStringDateConverter;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public abstract class SurveyBaseVM extends BaseVM {
	
	public static final String ERRORS_IN_PAGE_MESSAGE_KEY = "global.message.errors_in_page";
	private static final String DATE_FORMAT = Labels.getLabel("global.date_format");

	protected XMLStringDateConverter xmlStringDateConverter = new XMLStringDateConverter();
	
	
	@WireVariable
	protected CollectSurvey survey;

	protected String currentLanguageCode;
	
	private boolean currentFormValid;
	
	public SurveyBaseVM() {
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
	@NotifyChange("tabSets")
	public void tabSetsUpdated() {}
	
	@GlobalCommand
	@NotifyChange("currentFormValid")
	public void currentFormValidated(@BindingParam("valid") boolean valid) {
		currentFormValid = valid;
	}
	
	public void dispatchCurrentFormValidatedCommand(boolean valid) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("valid", valid);
		BindUtils.postGlobalCommand(null, null, "currentFormValidated", args);
	}
	
	public boolean checkCurrentFormValid() {
		if ( currentFormValid ) {
			return true;
		} else {
			MessageUtil.showWarning(ERRORS_IN_PAGE_MESSAGE_KEY);
			return false;
		}
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
		return DATE_FORMAT;
	}

	public List<Object> getVersionsForCombo() {
		CollectSurvey survey = getSurvey();
		List<Object> result = new ArrayList<Object>(survey.getVersions());
		result.add(0, FormObject.VERSION_EMPTY_SELECTION);
		return new BindingListModelList<Object>(result, false);
	}
	
	public List<Integer> getVersionIdsForCombo() {
		CollectSurvey survey = getSurvey();
		List<ModelVersion> versions = survey.getVersions();
		List<Integer> result = new ArrayList<Integer>();
		result.add(0, -1);
		for (ModelVersion modelVersion : versions) {
			result.add(modelVersion.getId());
		}
		return new BindingListModelList<Integer>(result, false);
	}
	
	public String getVersionLabel(int id) {
		if ( id > 0 ) {
			CollectSurvey survey = getSurvey();
			ModelVersion version = survey.getVersionById(id);
			String result = version.getLabel(currentLanguageCode);
			if ( result == null && isDefaultLanguage() ) {
				result = version.getLabel(null);
			}
			return result;
		} else {
			return Labels.getLabel(EMPTY_OPTION);
		}
			
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

	public boolean isDefaultLanguage() {
		CollectSurvey survey = getSurvey();
		if ( survey != null ) {
			String defaultLanguageCode = survey.getDefaultLanguage();
			return currentLanguageCode != null && currentLanguageCode.equals(defaultLanguageCode);
		} else {
			return false;
		}
	}
	
	public String getCurrentLanguageCode() {
		return currentLanguageCode;
	}
	
	public boolean isCurrentFormValid() {
		return currentFormValid;
	}

}
