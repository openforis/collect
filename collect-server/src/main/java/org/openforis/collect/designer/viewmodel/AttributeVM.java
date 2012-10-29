package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.model.LabelKeys.CHECK_FLAG_ERROR;
import static org.openforis.collect.designer.model.LabelKeys.CHECK_FLAG_WARNING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.AttributeDefinitionFormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.CheckType;
import org.openforis.collect.designer.util.Resources;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.Check.Flag;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
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
	protected List<AttributeDefault> attributeDefaults;
	protected AttributeDefault selectedAttributeDefault;

	private boolean editingNewCheck;
	private Check<?> editedCheck;
	private Check<?> selectedCheck;
	private List<Check<?>> checks;

	private Window checkPopUp;
	
	@SuppressWarnings("unchecked")
	@Override
	protected SurveyObjectFormObject<T> createFormObject() {
		AttributeType attributeTypeEnum = AttributeType.valueOf(editedItem);
		formObject = (AttributeDefinitionFormObject<T>) NodeDefinitionFormObject.newInstance(attributeTypeEnum);
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
			AttributeDefinitionFormObject<T> fo = (AttributeDefinitionFormObject<T>) formObject;
			checks = new ArrayList<Check<?>>(editedItem.getChecks());
			fo.setChecks(checks);
			tempFormObject.setField(CHECKS_FIELD, checks);
		} else {
			checks = null;
		}
	}

	protected void initAttributeDefaults() {
		if ( editedItem != null ) {
			attributeDefaults = ((AttributeDefinitionFormObject<T>) formObject).getAttributeDefaults();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
		} else {
			attributeDefaults = null;
		}
	}
	
	@Command
	@NotifyChange("attributeDefaults")
	public void addAttributeDefault() {
		if ( attributeDefaults == null ) {
			initAttributeDefaultsList();
		}
		AttributeDefault attributeDefault = new AttributeDefault();
		attributeDefaults.add(attributeDefault);
	}
	
	@Command
	@NotifyChange({"selectedAttributeDefault","attributeDefaults"})
	public void deleteAttributeDefault() {
		attributeDefaults.remove(selectedAttributeDefault);
		selectedAttributeDefault = null;
	}
	
	@Command
	@NotifyChange("selectedAttributeDefault")
	public void selectAttributeDefault(@BindingParam("attributeDefault") AttributeDefault attributeDefault) {
		selectedAttributeDefault = attributeDefault;
	}
	
	@Override
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		notifyChange("attributeDefaults","numericAttributePrecisions");
	}
	
	protected void initAttributeDefaultsList() {
		if ( attributeDefaults == null ) {
			attributeDefaults = new ArrayList<AttributeDefault>();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			((AttributeDefinitionFormObject<?>) formObject).setAttributeDefaults(attributeDefaults);
		}
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
		editedCheck = selectedCheck;
		editingNewCheck = false;
		openCheckEditPopUp();
	}
	
	@Command
	@NotifyChange({"selectedCheck","checks"})
	public void deleteCheck() {
		checks.remove(selectedCheck);
		selectedCheck = null;
	}
	
	@Command
	@NotifyChange("selectedAttributeDefault")
	public void selectCheck(@BindingParam("check") Check<?> check) {
		selectedCheck = check;
	}
	
	@GlobalCommand
	public void applyChangesToEditedCheck(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( editedCheck != null && checkCurrentFormValid() ) {
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
			if ( editingNewCheck ) {
				editedItem.removeCheck(editedCheck);
			}
			closeCheckEditPopUp(binder);
			editedCheck = null;
		}
	}
	
	protected void closeCheckEditPopUp(Binder binder) {
		closePopUp(checkPopUp);
		checkPopUp = null;
		validateForm(binder);
	}

	protected void openCheckEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("parentDefinition", editedItem);
		args.put("newItem", editingNewCheck);
		args.put("check", editedCheck);
		checkPopUp = openPopUp(Resources.Component.CHECK_POPUP.getLocation(), true, args);
	}
	
	protected void initCheckList() {
		if ( checks == null ) {
			checks = new ArrayList<Check<?>>();
			tempFormObject.setField(CHECKS_FIELD, checks);
			((AttributeDefinitionFormObject<?>) formObject).setAttributeDefaults(attributeDefaults);
		}
	}
	
	public String getAttributeType() {
		if ( editedItem == null ) {
			return null;
		} else {
			AttributeType type = AttributeType.valueOf(editedItem);
			return type.name().toLowerCase();
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
		return Arrays.asList(values);
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
	
}
