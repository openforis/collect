package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.form.AttributeDefinitionFormObject.ATTRIBUTE_DEFAULTS_FIELD;
import static org.openforis.collect.designer.form.AttributeDefinitionFormObject.KEY_FIELD;
import static org.openforis.collect.designer.form.AttributeDefinitionFormObject.MEASUREMENT_FIELD;
import static org.openforis.collect.designer.form.NodeDefinitionFormObject.MULTIPLE_FIELD;
import static org.openforis.collect.designer.form.NodeDefinitionFormObject.REQUIRENESS_FIELD;
import static org.openforis.collect.designer.model.LabelKeys.CHECK_FLAG_ERROR;
import static org.openforis.collect.designer.model.LabelKeys.CHECK_FLAG_WARNING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.AttributeDefinitionFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject.RequirenessType;
import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.designer.model.CheckType;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmParams;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.SchemaObjectSelectorPopUpVM.NodeSelectedEvent;
import org.openforis.collect.manager.validation.SurveyValidator.ReferenceableKeyAttributeHelper;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.Check.Flag;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.PatternCheck;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AttributeVM<T extends AttributeDefinition> extends NodeDefinitionVM<T> {

	private static final String EDITED_NODE_KEY_CHANGING_GLOBAL_COMMAND = "editedNodeKeyChanging";
	private static final String EDITED_NODE_CALCULATED_PROPERTY_CHANGING_GLOBAL_COMMAND = "editedNodeCalculatedPropertyChanging";
	
	private List<Check<?>> checks;
	private boolean editingNewCheck;
	private Check<?> editedCheck;
	private Check<?> selectedCheck;

	protected List<AttributeDefault> attributeDefaults;
	private Boolean editingNewAttributeDefault;
	private AttributeDefault editedAttributeDefault;
	protected AttributeDefault selectedAttributeDefault;

	private Window checkPopUp;
	private Window attributeDefaultPopUp;

	public AttributeVM() {
		super();
		fieldLabelKeyPrefixes.addAll(Arrays.asList("survey.schema.attribute"));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected FormObject<T> createFormObject() {
		AttributeType attributeTypeEnum = AttributeType.valueOf(editedItem);
		formObject = (AttributeDefinitionFormObject<T>) NodeDefinitionFormObject.newInstance(parentEntity, attributeTypeEnum);
		return formObject;
	}

	@Override
	@NotifyChange({"editedItem","formObject","tempFormObject","checks"})
	public void setEditedItem(T editedItem) {
		super.setEditedItem(editedItem);
		initAttributeDefaults();
		initChecks();
	}
	
	@Override
	public List<Map<String, String>> getDependentNodes() {
		List<Map<String, String>> result = super.getDependentNodes();
		//result.addAll(getDependentNodeInfos("check", editedItem.getCheckDependentDefinitions()));
		return result;
	}

	protected void initChecks() {
		if ( editedItem == null ) {
			checks = null;
		} else {
			checks = new ArrayList<Check<?>>(editedItem.getChecks());
			((AttributeDefinitionFormObject<T>) formObject).setChecks(checks);
			setTempFormObjectFieldValue(AttributeDefinitionFormObject.CHECKS_FIELD, checks);
		}
	}

	@Override
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		notifyChange("attributeDefaults","precisions");
	}
	
	@Command
	public void addCheck(@BindingParam("checkType") String checkType) {
		if (checkCanAddCheck()) {
			CheckType type = CheckType.valueOf(checkType.toUpperCase(Locale.ENGLISH));
			editingNewCheck = true;
			editedCheck = CheckType.createCheck(type);
			openCheckEditPopUp();
		}
	}
	
	@Command
	public void editCheck() {
		editingNewCheck = false;
		editedCheck = selectedCheck;
		openCheckEditPopUp();
	}
	
	@Command
	public void deleteCheck() {
		ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				editedItem.removeCheck(selectedCheck);
				selectedCheck = null;
				initChecks();
				notifyChange("selectedCheck","checks");
			}
		}, "survey.schema.node.check.confirm_delete");
		params.setOkLabelKey("global.delete_item");
		MessageUtil.showConfirm(params);
	}
	
	@Command
	@NotifyChange("selectedAttributeDefault")
	public void selectCheck(@BindingParam("check") Check<?> check) {
		selectedCheck = check;
	}
	
	@Command
	public void keyChanged(@ContextParam(ContextType.BINDER) Binder binder,
			@BindingParam("key") boolean key ) {
		if (key) {
			setTempFormObjectFieldValue(REQUIRENESS_FIELD, RequirenessType.ALWAYS_REQUIRED.name());
		} else {
			setTempFormObjectFieldValue(MEASUREMENT_FIELD, false);
		}
		dispatchApplyChangesCommand(binder);
		dispatchKeyChangingCommand(key);
	}

	private void dispatchKeyChangingCommand(Boolean key) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("item", editedItem);
		if (key != null) {
			args.put("key", key);
		}
		BindUtils.postGlobalCommand(null, null, EDITED_NODE_KEY_CHANGING_GLOBAL_COMMAND, args);
	}
	
	@Command
	public void calculatedChanged(@ContextParam(ContextType.BINDER) Binder binder, @BindingParam("changed") boolean changed) {
		Boolean key = getTempFormObjectFieldValue(KEY_FIELD);
		setTempFormObjectFieldValue(MULTIPLE_FIELD, false);
		setTempFormObjectFieldValue("showInUI", true);
		setTempFormObjectFieldValue("includeInDataExport", Annotation.INCLUDE_IN_DATA_EXPORT.getDefaultValue());
		setTempFormObjectFieldValue("calculatedOnlyOneTime", key);
		setTempFormObjectFieldValue("editable", Annotation.EDITABLE.getDefaultValue());
		setTempFormObjectFieldValue("phaseToApplyDefaultValue", ((Step) Annotation.PHASE_TO_APPLY_DEFAULT_VALUE.getDefaultValue()).name());
		dispatchCalculatedPropertyChangingCommand(changed);
		dispatchApplyChangesCommand(binder);
	}
	
	private void dispatchCalculatedPropertyChangingCommand(boolean calculated) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("item", editedItem);
		args.put("calculated", calculated);
		BindUtils.postGlobalCommand(null, null, EDITED_NODE_CALCULATED_PROPERTY_CHANGING_GLOBAL_COMMAND, args);
	}
	
	protected void openCheckEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("parentDefinition", editedItem);
		args.put("newItem", editingNewCheck);
		args.put("check", editedCheck);
		checkPopUp = openPopUp(Resources.Component.CHECK_POPUP.getLocation(), true, args);
	}
	
	@GlobalCommand
	public void applyChangesToEditedCheck(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( editedCheck != null && checkCanLeaveForm() ) {
			if ( editingNewCheck ) {
				editedItem.addCheck(editedCheck);
			}
			closeCheckEditPopUp(binder);
			editedCheck = null;
			initChecks();
			notifyChange("checks");
		}
	}
	
	@GlobalCommand
	public void cancelChangesToEditedCheck(@ContextParam(ContextType.BINDER) Binder binder) {
		//TODO confirm if there are not committed changes 
		if ( editedCheck != null ) {
			closeCheckEditPopUp(binder);
			editedCheck = null;
		}
	}
	
	protected void closeCheckEditPopUp(Binder binder) {
		closePopUp(checkPopUp);
		checkPopUp = null;
		validateForm(binder);
	}
	
	protected void initAttributeDefaults() {
		if ( editedItem == null ) {
			attributeDefaults = null;
		} else {
			attributeDefaults =  new ArrayList<AttributeDefault>(editedItem.getAttributeDefaults());
			setTempFormObjectFieldValue(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			((AttributeDefinitionFormObject<?>) formObject).setAttributeDefaults(attributeDefaults);
		}
		notifyChange("formObject", "tempFormObject", "attributeDefaults");
	}

	@Command
	@NotifyChange("attributeDefaults")
	public void addAttributeDefault() {
		if ( checkCanInsertAttributeDefault() ) {
			editingNewAttributeDefault = true;
			editedAttributeDefault = new AttributeDefault();
			openAttributeDefaultEditPopUp();
		}
	}

	protected boolean checkCanInsertAttributeDefault() {
		if (! checkNodeAttached()) {
			return false;
		}
		if ( attributeDefaults != null && ! attributeDefaults.isEmpty() ) {
			String lastItemCondition = attributeDefaults.get(attributeDefaults.size() - 1).getCondition(); 
			if ( StringUtils.isBlank(lastItemCondition) || lastItemCondition.trim().equalsIgnoreCase("true()") ) {
				MessageUtil.showWarning("survey.schema.attribute.attribute_default.cannot_insert.item_without_condition_found");
				return false;
			}
		}
		return true;
	}
	
	private boolean checkCanAddCheck() {
		return checkNodeAttached();
	}

	@Command
	public void editAttributeDefault() {
		editingNewAttributeDefault = false;
		editedAttributeDefault = selectedAttributeDefault;
		openAttributeDefaultEditPopUp();
	}
	
	@Command
	@NotifyChange({"selectedAttributeDefault","attributeDefaults"})
	public void deleteAttributeDefault() {
		ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				editedItem.removeAttributeDefault(selectedAttributeDefault);
				selectedAttributeDefault = null;
				initAttributeDefaults();
				notifyChange("selectedAttributeDefault","attributeDefaults");
			}
		}, "survey.schema.attribute.attribute_default.confirm_delete");
		params.setOkLabelKey("global.delete_item");
		MessageUtil.showConfirm(params);
	}
	
	@Command
	@NotifyChange("selectedAttributeDefault")
	public void selectAttributeDefault(@BindingParam("attributeDefault") AttributeDefault attributeDefault) {
		selectedAttributeDefault = attributeDefault;
	}

	protected void openAttributeDefaultEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("parentDefinition", editedItem);
		args.put("newItem", editingNewAttributeDefault);
		args.put("attributeDefault", editedAttributeDefault);
		attributeDefaultPopUp = openPopUp(Resources.Component.ATTRIBUTE_DEFAULT_POPUP.getLocation(), true, args);
	}
	
	@GlobalCommand
	public void applyChangesToEditedAttributeDefault(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( editedAttributeDefault != null && checkCanLeaveForm() ) {
			closeAttributeDefaultEditPopUp(binder);
			editedAttributeDefault = null;
			initAttributeDefaults();
			notifyChange("attributeDefaults");
		}
	}

	@GlobalCommand
	public void cancelChangesToEditedAttributeDefault(@ContextParam(ContextType.BINDER) Binder binder) {
		//TODO confirm if there are not committed changes 
		if ( editedAttributeDefault != null ) {
			closeAttributeDefaultEditPopUp(binder);
			editedAttributeDefault = null;
		}
	}
	
	protected void closeAttributeDefaultEditPopUp(Binder binder) {
		closePopUp(attributeDefaultPopUp);
		attributeDefaultPopUp = null;
		validateForm(binder);
	}
	
	@Command
	@NotifyChange({"attributeDefaults"})
	public void moveSelectedAttributeDefaultUp() {
		moveSelectedAttributeDefault(true);
	}
	
	@Command
	@NotifyChange({"attributeDefaults"})
	public void moveSelectedAttributeDefaultDown() {
		moveSelectedAttributeDefault(false);
	}
	
	protected void moveSelectedAttributeDefault(boolean up) {
		int indexFrom = getSelectedAttributeDefaultIndex();
		int indexTo = up ? indexFrom - 1: indexFrom + 1;
		moveSelectedAttributeDefault(indexTo);
	}
	
	protected int getSelectedAttributeDefaultIndex() {
		List<?> items = editedItem.getAttributeDefaults();
		int index = items.indexOf(selectedAttributeDefault);
		return index;
	}

	protected void moveSelectedAttributeDefault(int indexTo) {
		editedItem.moveAttributeDefault(selectedAttributeDefault, indexTo);
		initAttributeDefaults();
	}
	
	@DependsOn({"attributeDefaults","selectedAttributeDefault"})
	public boolean isMoveSelectedAttributeDefaultUpDisabled() {
		return isMoveSelectedAttributeDefaultDisabled(true);
	}
	
	@DependsOn({"attributeDefaults","selectedAttributeDefault"})
	public boolean isMoveSelectedAttributeDefaultDownDisabled() {
		return isMoveSelectedAttributeDefaultDisabled(false);
	}
	
	protected boolean isMoveSelectedAttributeDefaultDisabled(boolean up) {
		if ( selectedAttributeDefault == null ) {
			return true;
		} else {
			List<AttributeDefault> siblings = editedItem.getAttributeDefaults();
			int index = siblings.indexOf(selectedAttributeDefault);
			return up ? index <= 0: index < 0 || index >= siblings.size() - 1;
		}
	}
	
	@Command
	public void openReferencedAttributeSelector(@ContextParam(ContextType.BINDER) final Binder binder) {
		ReferenceableKeyAttributeHelper referenceableKeyAttributeHelper = new ReferenceableKeyAttributeHelper(editedItem);
		final Set<EntityDefinition> referenceableEntityDefinitions = referenceableKeyAttributeHelper.determineReferenceableEntities();
		final Set<AttributeDefinition> selectableAttributes = referenceableKeyAttributeHelper.determineReferenceableAttributes();
				
		if ( selectableAttributes.isEmpty() ) {
			MessageUtil.showWarning("survey.schema.attribute.no_referenceable_attributes_available");
		} else {
			Predicate<SurveyObject> includedNodePredicate = new Predicate<SurveyObject>() {
				public boolean evaluate(SurveyObject item) {
					EntityDefinition parentEntity;
					if (item instanceof UITab) {
						parentEntity = survey.getUIOptions().getParentEntityForAssignedNodes((UITab) item);
					} else {
						parentEntity = ((NodeDefinition) item).getParentEntityDefinition(); 
					}
					for (EntityDefinition entityDef : referenceableEntityDefinitions) {
						if (parentEntity == entityDef || parentEntity.isAncestorOf(entityDef)) {
							return true;
						}
					}
					return false;
				}
			};
			Predicate<SurveyObject> disabledNodePredicate = new Predicate<SurveyObject>() {
				public boolean evaluate(SurveyObject item) {
					return ! selectableAttributes.contains(item);
				}
			};
			String title = Labels.getLabel("survey.schema.attribute.select_attribute_referenced_by", new String[]{editedItem.getName()});
			final Window parentSelectorPopUp = SchemaObjectSelectorPopUpVM.openPopup(title, false,
					editedItem.getRootEntity(), null, includedNodePredicate,
					false, false, disabledNodePredicate, null,
					editedItem.getReferencedAttribute(), true);
			parentSelectorPopUp.addEventListener(SchemaObjectSelectorPopUpVM.NODE_SELECTED_EVENT_NAME, new EventListener<NodeSelectedEvent>() {
				public void onEvent(NodeSelectedEvent event) throws Exception {
					AttributeDefinition referencedAttribute = (AttributeDefinition) event.getSelectedItem();
					AttributeDefinitionFormObject<?> fo = (AttributeDefinitionFormObject<?>) formObject;
					fo.setReferencedAttributePath(referencedAttribute == null ? null : referencedAttribute.getPath());
					notifyChange("formObject");
					dispatchApplyChangesCommand(binder);
					closePopUp(parentSelectorPopUp);
				}
			});
		}
	}
	
	@Command
	public void generateEntityAlias() {
		AttributeDefinitionFormObject<?> fo = (AttributeDefinitionFormObject<?>) formObject;
		String referencedAttributePath = fo.getReferencedAttributePath();
		EntityDefinition sourceDef = editedItem.getParentEntityDefinition();
		
		String aliasName = sourceDef + "_alias";

		NodeDefinition referencedAttributeDef = editedItem.getDefinitionByPath(referencedAttributePath);
		EntityDefinition parentDef = referencedAttributeDef.getNearestAncestorMultipleEntity();
		if (parentDef.containsChildDefinition(aliasName)) {
			MessageUtil.showError("survey.schema.attribute.generate_entity_alias.error.alias_already_existing", aliasName, parentDef.getName());
		} else {
			EntityDefinition aliasDef = schemaUpdater.generateAlias(sourceDef, editedItem.getName(), parentDef, referencedAttributeDef.getName());
			((CollectSurvey) aliasDef.getSurvey()).getUIOptions().setLayout(aliasDef, Layout.TABLE);
			aliasDef.rename(aliasName);
			dispatchSchemaChangedCommand();
			MessageUtil.showInfo("survey.schema.attribute.generate_entity_alias.generation_successfull", aliasName, parentDef.getName());
		}
	}
	
	public List<AttributeDefault> getAttributeDefaults() {
		return attributeDefaults;
	}
	
	public AttributeDefault getSelectedAttributeDefault() {
		return selectedAttributeDefault;
	}

	public void setSelectedAttributeDefault(
			AttributeDefault selectedAttributeDefault) {
		this.selectedAttributeDefault = selectedAttributeDefault;
	}

	public Check<?> getSelectedCheck() {
		return selectedCheck;
	}
	
	public void setSelectedCheck(Check<?> selectedCheck) {
		this.selectedCheck = selectedCheck;
	}
	
	public List<Check<?>> getChecks() {
		return checks;
	}

	public List<CheckType> getCheckTypes() {
		return CheckType.compatibleValues(AttributeType.valueOf(editedItem));
	}
	
	public String getCheckTypeLabel(Check<?> check) {
		CheckType type = CheckType.valueOf(check);
		return type.getLabel();
	}
	
	public String getCheckFlagLabel(Check<?> check) {
		Flag flag = check.getFlag();
		switch(flag) {
		case ERROR:
			return Labels.getLabel(CHECK_FLAG_ERROR);
		case WARN:
			return Labels.getLabel(CHECK_FLAG_WARNING);
		default:
			return null;
		}
	}
	
	public String getCheckMessage(Check<?> check) {
		String result = check.getMessage(currentLanguageCode);
		if ( result == null ) {
			CheckType type = CheckType.valueOf(check);
			result = type.getDefaultMessage();
		}
		return result;
	}
	
	public String getCheckExpressionPreview(Check<?> check) {
		if ( check instanceof ComparisonCheck ) {
			return ((ComparisonCheck) check).getExpression();
		} else if ( check instanceof CustomCheck ) {
			return ((CustomCheck) check).getExpression();
		} else if ( check instanceof DistanceCheck ) {
			return getDistanceCheckExpressionPreview((DistanceCheck) check);
		} else if ( check instanceof PatternCheck ) {
			return ((PatternCheck) check).getRegularExpression();
		} else if ( check instanceof UniquenessCheck ) {
			return ((UniquenessCheck) check).getExpression();
		} else {
			return null;
		}
	}
	
	public String getCheckCondition(Check<?> check) {
		return check.getCondition();
	}

	private String getDistanceCheckExpressionPreview(DistanceCheck dc) {
		List<String> parts = new ArrayList<String>();
		if ( StringUtils.isNotBlank(dc.getMinDistanceExpression()) ) {
			parts.add(Labels.getLabel("survey.schema.node.check.distance.minDistanceExpressionPreview",
					new String[]{dc.getDestinationPointExpression(), 
					dc.getMinDistanceExpression()}));
		}
		if ( StringUtils.isNotBlank(dc.getMaxDistanceExpression()) ) {
			parts.add(Labels.getLabel("survey.schema.node.check.distance.maxDistanceExpressionPreview",
					new String[]{dc.getDestinationPointExpression(), 
					dc.getMaxDistanceExpression()}));
		}
		return StringUtils.join(parts, "\n");
	}

}
