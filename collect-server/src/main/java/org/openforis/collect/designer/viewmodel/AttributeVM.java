package org.openforis.collect.designer.viewmodel;

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
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.validation.Check;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Path;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class AttributeVM<T extends AttributeDefinition> extends SurveyObjectBaseVM<T> {

	private static final String FORM_CONTAINER_ID = "nodeFormContainer";
	private static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	private static final String CHECKS_FIELD = null;
	
//	private EntityDefinition parentEntity;
	protected Form tempFormObject;
	protected List<AttributeDefault> attributeDefaults;
	protected AttributeDefault selectedAttributeDefault;

	private boolean editingNewCheck;
	private Check<?> editedCheck;
	private Check<?> selectedCheck;
	private List<Check<?>> checks;

	private Window checkPopUp;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") T attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init();
		if ( attributeDefn != null ) {
//			this.parentEntity = parentEntity;
			this.newItem = newItem;
			setEditedItem(attributeDefn);
		}
	}
	
	@Override
	protected List<T> getItemsInternal() {
		return null;
	}

	@Override
	protected void moveSelectedItem(int indexTo) {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SurveyObjectFormObject<T> createFormObject() {
		AttributeType attributeTypeEnum = AttributeType.valueOf(editedItem);
		formObject = (AttributeDefinitionFormObject<T>) NodeDefinitionFormObject.newInstance(attributeTypeEnum);
		tempFormObject = new SimpleForm();
		return formObject;
	}

	@Override
	protected T createItemInstance() {
		return null;
	}

	@Override
	protected void addNewItemToSurvey() {
		//do nothing
	}

	@Override
	protected void deleteItemFromSurvey(AttributeDefinition item) {
		//do nothing
	}
	
	@Override
	@Command
	public void applyChanges() {
		//do not call super, postpone to commitChanges command
		super.changed = true;
	}
	
	public void commitChanges() {
		super.applyChanges();
	}
	
	@Override
	@NotifyChange({"editedItem","formObject","tempFormObject"})
	public void setEditedItem(T editedItem) {
		super.setEditedItem(editedItem);
		if ( editedItem != null ) {
			attributeDefaults = ((AttributeDefinitionFormObject<T>) formObject).getAttributeDefaults();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			
			checks = ((AttributeDefinitionFormObject<T>) formObject).getChecks();
			tempFormObject.setField(CHECKS_FIELD, checks);
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
			IdSpace popUpIdSpace = checkPopUp.getSpaceOwner();
			Component formContainer = Path.getComponent(popUpIdSpace, "detailsInclude/nodeFormContainer");
			Binder checkFormBinder = (Binder) formContainer.getAttribute("binder");
			AttributeVM<?> checkFormViewModel = (AttributeVM<?>) checkFormBinder.getViewModel();
			checkFormViewModel.commitChanges();
			if ( editingNewCheck ) {
				editedItem.addCheck(editedCheck);
			}
			closeCheckEditPopUp(binder);
			editedCheck = null;
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
	
	protected void validateForm(@ContextParam(ContextType.BINDER) Binder binder) {
		Component view = binder.getView();
		IdSpace currentIdSpace = view.getSpaceOwner();
		Component formComponent = Path.getComponent(currentIdSpace, FORM_CONTAINER_ID);
		Binder formComponentBinder = (Binder) formComponent.getAttribute("binder");
		formComponentBinder.postCommand("applyChanges", null);
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
	
}
