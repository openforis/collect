package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.model.LabelKeys.CHECK_FLAG_ERROR;
import static org.openforis.collect.designer.model.LabelKeys.CHECK_FLAG_WARNING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.AttributeDefinitionFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.CheckType;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.Check.Flag;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AttributeVM<T extends AttributeDefinition> extends NodeDefinitionVM<T> {

	private static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	private static final String CHECKS_FIELD = null;
	
//	private EntityDefinition parentEntity;

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

	@SuppressWarnings("unchecked")
	@Override
	protected FormObject<T> createFormObject() {
		AttributeType attributeTypeEnum = AttributeType.valueOf(editedItem);
		formObject = (AttributeDefinitionFormObject<T>) NodeDefinitionFormObject.newInstance(parentEntity, attributeTypeEnum);
		tempFormObject = new SimpleForm();
		return formObject;
	}

	@Override
	@NotifyChange({"editedItem","formObject","tempFormObject"})
	public void setEditedItem(T editedItem) {
		super.setEditedItem(editedItem);
		initAttributeDefaults();
		initChecks();
	}

	protected void initChecks() {
		if ( editedItem != null ) {
			checks = new ArrayList<Check<?>>(editedItem.getChecks());
			updateFormObjectChecks();
		} else {
			checks = null;
		}
	}

	protected void updateFormObjectChecks() {
		((AttributeDefinitionFormObject<T>) formObject).setChecks(checks);
		tempFormObject.setField(CHECKS_FIELD, checks);
	}

	@Override
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		notifyChange("attributeDefaults","precisions");
	}
	
	@Command
	public void addCheck(@BindingParam("checkType") String checkType) {
		CheckType type = CheckType.valueOf(checkType.toUpperCase());
		editingNewCheck = true;
		editedCheck = CheckType.createCheck(type);
		openCheckEditPopUp();
	}
	
	@Command
	public void editCheck() {
		editingNewCheck = false;
		editedCheck = selectedCheck;
		openCheckEditPopUp();
	}
	
	@Command
	public void deleteCheck() {
		MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				editedItem.removeCheck(selectedCheck);
				selectedCheck = null;
				initChecks();
				notifyChange("selectedCheck","checks");
			}
		}, "survey.schema.node.check.confirm_delete");
	}
	
	@Command
	@NotifyChange("selectedAttributeDefault")
	public void selectCheck(@BindingParam("check") Check<?> check) {
		selectedCheck = check;
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
		if ( editedItem != null ) {
			attributeDefaults =  new ArrayList<AttributeDefault>(editedItem.getAttributeDefaults());
			updateFormObjectAttributeDefaults();
		} else {
			attributeDefaults = null;
		}
		notifyChange("attributeDefaults");
	}

	protected void updateFormObjectAttributeDefaults() {
		tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
		((AttributeDefinitionFormObject<?>) formObject).setAttributeDefaults(attributeDefaults);
	}
	
	@Command
	@NotifyChange("attributeDefaults")
	public void addAttributeDefault() {
		editingNewAttributeDefault = true;
		editedAttributeDefault = new AttributeDefault();
		openAttributeDefaultEditPopUp();
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
		MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				editedItem.removeAttributeDefault(selectedAttributeDefault);
				selectedAttributeDefault = null;
				initAttributeDefaults();
				notifyChange("selectedAttributeDefault","attributeDefaults");
			}
		}, "survey.schema.attribute.attribute_default.confirm_delete");
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

	public List<AttributeDefault> getAttributeDefaults() {
		return attributeDefaults;
	}
	
	public Form getTempFormObject() {
		return tempFormObject;
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
		CheckType[] values = CheckType.values();
		List<CheckType> list = new ArrayList<CheckType>(Arrays.asList(values));
		if ( !(editedItem instanceof CoordinateAttributeDefinition) ) {
			list.remove(CheckType.DISTANCE);
		}
		return list;
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
	
}
