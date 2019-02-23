/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.model.LabelKeys.CONFIRM_LEAVE_PAGE_WITH_ERRORS;
import static org.openforis.collect.designer.model.LabelKeys.EMPTY_OPTION;
import static org.openforis.collect.designer.model.LabelKeys.ERRORS_IN_PAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.metamodel.SchemaUpdater;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.commons.lang.Strings;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelList;

import liquibase.util.StringUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SurveyBaseVM extends BaseVM {
	
	public enum SurveyType {
		TEMPORARY, PUBLISHED
	}
	
	public static final String VERSIONS_UPDATED_GLOBAL_COMMAND = "versionsUpdated";
	public static final String UNDO_LAST_CHANGES_GLOBAL_COMMAND = "undoLastChanges";
	public static final String SURVEY_CHANGED_GLOBAL_COMMAND = "surveyChanged";
	public static final String SCHEMA_CHANGED_GLOBAL_COMMAND = "schemaChanged";
	public static final String NODE_CONVERTED_GLOBAL_COMMAND = "nodeConverted";
	public static final String SURVEY_SAVED_GLOBAL_COMMAND = "surveySaved";
	public static final String VALIDATE_ALL_GLOBAL_COMMAND = "validateAll";

	public static final String DATE_FORMAT = Labels.getLabel("global.date_format");
	
	@WireVariable
	protected CollectSurvey survey;
	
	@WireVariable
	private ExpressionValidator expressionValidator;

	protected String currentLanguageCode;
	
	private boolean currentFormBlocking;
	private boolean currentFormValid;
	private Map<String, List<String>> currentFormValidationMessages;
	protected List<String> fieldLabelKeyPrefixes;
	protected SchemaUpdater schemaUpdater;
	
	public SurveyBaseVM() {
		currentFormValid = true;
		currentFormBlocking = false;
		fieldLabelKeyPrefixes = new ArrayList<String>();
	}

	@Override
	public void init() {
		super.init();
		initSurvey();
		initCurrentLanguageCode();
		
		schemaUpdater = new SchemaUpdater(survey);
	}
	
	private void initCurrentLanguageCode() {
		SessionStatus sessionStatus = getSessionStatus();
		currentLanguageCode = sessionStatus.getCurrentLanguageCode();
		notifyChange("currentLanguageCode");
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
			@BindingParam("blocking") Boolean blocking,
			@BindingParam("validationMessagesByField") Map<String, List<String>> validationMessagesByField) {
		currentFormValid = valid;
		currentFormBlocking = blocking != null && blocking.booleanValue();
		currentFormValidationMessages = validationMessagesByField;
		notifyChange("currentFormValid","currentFormBlocking");
	}
	
	@GlobalCommand
	public void undoLastChanges(@ContextParam(ContextType.VIEW) Component view) {
		undoLastChanges();
	}
	
	public void undoLastChanges() {
		dispatchCurrentFormValidatedCommand();
	}
	
	public void dispatchCurrentFormValidatedCommand() {
		dispatchCurrentFormValidatedCommand(true);
	}
	
	public void dispatchCurrentFormValidatedCommand(boolean valid) {
		dispatchCurrentFormValidatedCommand(valid, false);
	}

	public void dispatchCurrentFormValidatedCommand(boolean valid, boolean blocking) {
		dispatchCurrentFormValidatedCommand(valid, blocking, null);
	}

	public void dispatchCurrentFormValidatedCommand(boolean valid, boolean blocking, Map<String, List<String>> validationMessagesByField) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("valid", valid);
		args.put("blocking", blocking);
		args.put("validationMessagesByField", validationMessagesByField);
		BindUtils.postGlobalCommand(null, null, "currentFormValidated", args);
	}

	public void dispatchSurveyChangedCommand() {
		BindUtils.postGlobalCommand(null, null, SURVEY_CHANGED_GLOBAL_COMMAND, null);
	}

	public void dispatchSchemaChangedCommand() {
		BindUtils.postGlobalCommand(null, null, SCHEMA_CHANGED_GLOBAL_COMMAND, null);
		dispatchSurveyChangedCommand();
	}

	public void dispatchNodeConvertedCommand(final NodeDefinition nodeDef) {
		@SuppressWarnings("serial")
		HashMap<String, Object> args = new HashMap<String, Object>(){{
			put("node", nodeDef);
		}};
		BindUtils.postGlobalCommand(null, null, NODE_CONVERTED_GLOBAL_COMMAND, args);
		dispatchSurveyChangedCommand();
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
					public void onOk() {
						confirmHandler.onOk(true);
					}
					public void onCancel() {
						if ( confirmHandler instanceof CanLeaveFormCompleteConfirmHandler ) {
							((CanLeaveFormCompleteConfirmHandler) confirmHandler).onCancel();
						}
					}
				}, messageKey, new String[]{getValidationMessageSummary()}, "global.unsaved_changes", (Object[]) null, 
						"global.continue_and_loose_changes", "global.stay_on_this_page");
			}
		}
		return currentFormValid;
	}

	private String getValidationMessageSummary() {
		StringBuilder sb = new StringBuilder();
		Set<Entry<String,List<String>>> entrySet = currentFormValidationMessages.entrySet();
		int count = 0;
		for (Entry<String, List<String>> entry : entrySet) {
			String key = entry.getKey();
			String fieldLabel = getFieldLabel(key);
			List<String> messages = entry.getValue();
			sb.append(++ count + ") ");
			sb.append(fieldLabel);
			sb.append(": ");
			sb.append(Strings.htmlToText(StringUtils.join(messages, "; ")));
			sb.append("\n");
		}
		return sb.toString();
	}

	private String getFieldLabel(String key) {
		for ( String prefix : fieldLabelKeyPrefixes ) {
			String labelKey = prefix + "." + key; 
			String label = Labels.getLabel(labelKey);
			if ( label != null ) {
				return label;
			}
		}
		return key;
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
	
	public boolean isCollectEarthSurvey() {
		return survey != null && survey.getTarget() == SurveyTarget.COLLECT_EARTH;
	}
	
	public boolean isSurveyStored() {
		return getSurveyId() != null;
	}
	
	public boolean isSurveyPublished() {
		if ( survey == null ) {
			return false;
		} else {
			return survey.isPublished();
		}
	}
	
	public boolean isSurveyChanged() {
		SurveyEditVM rootVM = getRootVM();
		return rootVM == null ? false : rootVM.isSurveyChanged();
	}

	protected SurveyEditVM getRootVM() {
		return ComponentUtil.getAncestorViewModel(SurveyEditVM.class);
	}

	@GlobalCommand
	public void currentLanguageChanged() {
		initCurrentLanguageCode();
	}

	@GlobalCommand
	public void surveyChanged() {
		notifyChange("surveyChanged");
		notifyChange("rootEntities");
	}

	@GlobalCommand
	public void surveySaved() {
		notifyChange("surveyChanged");
	}

	public String getDateFormat() {
		return DATE_FORMAT;
	}

	public List<ModelVersion> getFormVersions() {
		List<ModelVersion> versions = getSurveyFormVersions();
		return new BindingListModelList<ModelVersion>(versions, false);
	}

	public List<Object> getFormVersionsWithEmptyOption() {
		List<ModelVersion> versions = getSurveyFormVersions();
		List<Object> result = new ArrayList<Object>(versions);
		result.add(0, FormObject.VERSION_EMPTY_SELECTION);
		return new BindingListModelList<Object>(result, false);
	}
	
	public List<Integer> getFormVersionIdsWithEmptyOption() {
		List<ModelVersion> versions = getSurveyFormVersions();
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
			return Collections.emptyList();
		} else {
			Schema schema = survey.getSchema();
			List<EntityDefinition> result = schema.getRootEntityDefinitions();
			return result;
		}
	}
	
	@DependsOn("rootEntities")
	public boolean isSingleRootEntityDefined() {
		List<EntityDefinition> rootEntities = getRootEntities();
		return rootEntities != null && rootEntities.size() == 1;
	}
	
	public List<CodeList> getCodeLists() {
		CollectSurvey survey = getSurvey();
		boolean includeSamplingDesignList = survey.getTarget() != SurveyTarget.COLLECT_EARTH;
		List<CodeList> result = new ArrayList<CodeList>(survey.getCodeLists(includeSamplingDesignList));
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
		return survey == null ? false : survey.isDefaultLanguage(currentLanguageCode);
	}
	
	public String getDefaultLanguageCode() {
		return survey == null ? null : survey.getDefaultLanguage();
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

	public ExpressionValidator getExpressionValidator() {
		return expressionValidator;
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
	
	public boolean isCurrentEditedSurvey(SurveySummary surveySummary) {
		SessionStatus sessionStatus = getSessionStatus();
		Integer editedPublishedSurveyId = sessionStatus.getPublishedSurveyId();
		Integer editedSurveyId = getSurveyId();
		if ( editedSurveyId == null ) {
			if ( editedPublishedSurveyId != null && surveySummary.isPublished() && 
					editedPublishedSurveyId.equals(surveySummary.getId()) ) {
				return true;
			} else {
				return false;
			}
		} else if ( surveySummary.getId().equals(editedSurveyId)) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> getEditableRecordStepNames() {
		return Arrays.asList(Step.CLEANSING.name(), Step.ENTRY.name());
	}

	private List<ModelVersion> getSurveyFormVersions() {
		CollectSurvey survey = getSurvey();
		if (survey == null) {
			//TODO session expired...?
			return Collections.emptyList();
		} else {
			return survey.getSortedVersions();
		}
	}
	
	protected boolean isSurveyRelatedToPublishedSurvey() {
		return survey.isTemporary() && survey.getPublishedId() != null;
	}
}
