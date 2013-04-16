/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.model.LabelKeys.CONFIRM_LEAVE_PAGE_WITH_ERRORS;
import static org.openforis.collect.designer.model.LabelKeys.EMPTY_OPTION;
import static org.openforis.collect.designer.model.LabelKeys.ERRORS_IN_PAGE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
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
	
	public static final String VERSIONS_UPDATED_GLOBAL_COMMAND = "versionsUpdated";
	public static final String UNDO_LAST_CHANGES_GLOBAL_COMMAND = "undoLastChanges";
	public static final String SURVEY_CHANGED_GLOBAL_COMMAND = "surveyChanged";
	public static final String VALIDATE_ALL_GLOBAL_COMMAND = "validateAll";

	public static final String DATE_FORMAT = Labels.getLabel("global.date_format");
	
	@WireVariable
	protected CollectSurvey survey;

	protected String currentLanguageCode;
	
	private boolean currentFormBlocking;
	private boolean currentFormValid;

	public SurveyBaseVM() {
		currentFormValid = true;
		currentFormBlocking = false;
	}

	@Init
	public void init() {
		initSurvey();
		initCurrentLanguageCode();
	}
	
	private void initCurrentLanguageCode() {
		SessionStatus sessionStatus = getSessionStatus();
		currentLanguageCode = sessionStatus.getCurrentLanguageCode();
		notifyChange("currentLanguageCode");
	}
	
	@GlobalCommand
	public void schemaChanged() {
		notifyChange("rootEntities");
	}
	
	@GlobalCommand
	public void versionsUpdated() {
		notifyChange("formVersions","formVersionsWithEmptyOption","formVersionIdsWithEmptyOption");
	}

	@GlobalCommand
	public void codeListsUpdated() {
		notifyChange("codeLists");
	}
	
	@GlobalCommand
	public void unitsUpdated() {
		notifyChange("units");
	}
	
	@GlobalCommand
	public void tabSetsUpdated() {
		notifyChange("tabSets");
	}
	
	@GlobalCommand
	public void currentFormValidated(@BindingParam("valid") boolean valid, 
			@BindingParam("blocking") Boolean blocking) {
		currentFormValid = valid;
		currentFormBlocking = blocking != null && blocking.booleanValue();
		notifyChange("currentFormValid","currentFormBlocking");
	}
	
	@GlobalCommand
	public void undoLastChanges() {
		dispatchCurrentFormValidatedCommand(true);
	}
	
	public void dispatchCurrentFormValidatedCommand(boolean valid) {
		dispatchCurrentFormValidatedCommand(valid, false);
	}

	public void dispatchCurrentFormValidatedCommand(boolean valid, boolean blocking) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("valid", valid);
		args.put("blocking", blocking);
		BindUtils.postGlobalCommand(null, null, "currentFormValidated", args);
	}

	public void dispatchSurveyChangedCommand() {
		BindUtils.postGlobalCommand(null, null, SURVEY_CHANGED_GLOBAL_COMMAND, null);
	}

	protected void dispatchValidateAllCommand() {
		BindUtils.postGlobalCommand(null, null, VALIDATE_ALL_GLOBAL_COMMAND, null);
	}

	public boolean checkCanLeaveForm() {
		return checkCanLeaveForm(null);
	}
	
	/**
	 * If the current form is valid, execute the onOk method of the specified confirmHandler and returns true, 
	 * otherwise shows a confirm message handled by the specified confirmHandler and returns false
	 * If the form is not valid and the confirmHandler is not specified, shows a warning message.
	 * 
	 * @param confirmHandler
	 * @return
	 */
	public boolean checkCanLeaveForm(CanLeaveFormConfirmHandler confirmHandler) {
		return checkCanLeaveForm(confirmHandler, CONFIRM_LEAVE_PAGE_WITH_ERRORS);
	}
	
	public boolean checkCanLeaveForm(final CanLeaveFormConfirmHandler confirmHandler, String messageKey) {
		if ( currentFormValid ) {
			if (confirmHandler != null ) {
				confirmHandler.onOk(false);
			}
		} else {
			if ( confirmHandler == null || currentFormBlocking ) {
				MessageUtil.showWarning(ERRORS_IN_PAGE);
			} else {
				MessageUtil.showConfirm(new MessageUtil.CompleteConfirmHandler() {
					@Override
					public void onOk() {
						confirmHandler.onOk(true);
					}
					
					@Override
					public void onCancel() {
						if ( confirmHandler instanceof CanLeaveFormCompleteConfirmHandler ) {
							((CanLeaveFormCompleteConfirmHandler) confirmHandler).onCancel();
						}
					}
				}, messageKey);
			}
		}
		return currentFormValid;
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

	public Integer getSurveyId() {
		if ( survey == null ) {
			return null;
		} else {
			return survey.getId();
		}
	}
	
	public boolean isSurveyPublished() {
		if ( survey == null ) {
			return false;
		} else {
			return survey.isPublished();
		}
	}

	@GlobalCommand
	public void currentLanguageChanged() {
		initCurrentLanguageCode();
	}

	public String getDateFormat() {
		return DATE_FORMAT;
	}

	public List<ModelVersion> getFormVersions() {
		CollectSurvey survey = getSurvey();
		if ( survey == null ) {
			//TODO session expired...?
			return null;
		} else {
			List<ModelVersion> result = new ArrayList<ModelVersion>(survey.getVersions());
			return new BindingListModelList<ModelVersion>(result, false);
		}
	}

	public List<Object> getFormVersionsWithEmptyOption() {
		CollectSurvey survey = getSurvey();
		List<Object> result = new ArrayList<Object>(survey.getVersions());
		result.add(0, FormObject.VERSION_EMPTY_SELECTION);
		return new BindingListModelList<Object>(result, false);
	}
	
	public List<Integer> getFormVersionIdsWithEmptyOption() {
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
			String result = null;
			if ( version != null ) {
				result = version.getLabel(currentLanguageCode);
				if ( result == null && isDefaultLanguage() ) {
					result = version.getLabel(null);
				}
				if ( result == null ) {
					result = version.getName();
				}
			}
			return result;
		} else {
			return Labels.getLabel(EMPTY_OPTION);
		}
	}
	
	public List<EntityDefinition> getRootEntities() {
		CollectSurvey survey = getSurvey();
		if ( survey == null ) {
			//TODO session expired...?
			return null;
		} else {
			Schema schema = survey.getSchema();
			List<EntityDefinition> result = schema.getRootEntityDefinitions();
			return result;
		}
	}
	
	public List<CodeList> getCodeLists() {
		CollectSurvey survey = getSurvey();
		List<CodeList> result = new ArrayList<CodeList>(survey.getCodeLists());
		result = sort(result);
		return new BindingListModelList<CodeList>(result, false);
	}
	
	public List<Unit> getUnits() {
		List<Unit> result = new ArrayList<Unit>(survey.getUnits());
		return new BindingListModelList<Unit>(result, false);
	}
	
	public String getUnitLabelFromPrecision(Precision precision) {
		Unit unit = precision.getUnit();
		return getUnitLabel(unit);
	}
	
	public String getUnitLabel(Unit unit) {
		String result = null;
		if ( unit != null ) {
			result = unit.getLabel(currentLanguageCode);
			if ( result == null ) {
				result = unit.getName();
			}
		}
		return result;
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
	
	public boolean isCurrentFormBlocking() {
		return currentFormBlocking;
	}

	public interface CanLeaveFormConfirmHandler {
		void onOk(boolean confirmed);
	}
	
	public interface CanLeaveFormCompleteConfirmHandler extends CanLeaveFormConfirmHandler {
		void onCancel();
	}
	
	protected List<CodeList> sort(List<CodeList> codeLists) {
		List<CodeList> result = new ArrayList<CodeList>(codeLists);
		Collections.sort(result, new Comparator<CodeList>() {
			@Override 
	        public int compare(CodeList c1, CodeList c2) {
	            return c1.getName().compareTo(c2.getName());
	        }
		});
		return result;
	}
	
	protected Map<String, String> createBasicModuleParameters() {
		Integer surveyId = getSurveyId();
		SessionStatus sessionStatus = getSessionStatus();
		Integer publishedSurveyId = sessionStatus.getPublishedSurveyId();
		boolean work = surveyId != null;
		Integer usedSurveyId = surveyId != null ? surveyId: publishedSurveyId;
		String surveyIdStr = usedSurveyId == null ? "": usedSurveyId.toString();
		String localeStr = sessionStatus.getCurrentLanguageCode();
		Map<String, String> result = new HashMap<String, String>();
		result.put("locale", localeStr);
		result.put("work", Boolean.toString(work));
		result.put("surveyId", surveyIdStr);
		return result;
	}

}
