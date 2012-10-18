package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.form.AttributeDefinitionFormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.NumericAttributeDefinitionFormObject;
import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeVM extends SurveyObjectBaseVM<AttributeDefinition> {

	private static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	private static final String NUMBER_ATTRIBUTE_PRECISIONS_FIELD = "precisions";
	
	private List<AttributeDefault> attributeDefaults;
	private List<Precision> numericAttributePrecisions;

	private Form tempFormObject;
	
	@Wire
	private Include attributeDetailsInclude;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
		 refreshNodeForm();
	}
	
	@Init
	public void init(@ExecutionArgParam("item") AttributeDefinition attributeDefn) {
		setEditedItem(attributeDefn);
	}
	
	protected void refreshNodeForm() {
		String type = getAttributeType();
		attributeDetailsInclude.setSrc("survey_edit/schema/attribute_" + type + ".zul");
	}

	@Override
	protected List<AttributeDefinition> getItemsInternal() {
		return null;
	}

	@Override
	protected void moveSelectedItem(int indexTo) {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SurveyObjectFormObject<AttributeDefinition> createFormObject() {
		AttributeType attributeTypeEnum = AttributeType.valueOf(editedItem);
		formObject = (AttributeDefinitionFormObject<AttributeDefinition>) NodeDefinitionFormObject.newInstance(attributeTypeEnum);
		tempFormObject = new SimpleForm();
		return formObject;
	}

	@Override
	protected AttributeDefinition createItemInstance() {
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
	@NotifyChange({"editedItem","formObject","tempFormObject"})
	public void setEditedItem(AttributeDefinition editedItem) {
		super.setEditedItem(editedItem);
		if ( editedItem != null ) {
			attributeDefaults = ((AttributeDefinitionFormObject<AttributeDefinition>) formObject).getAttributeDefaults();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			
			if ( formObject instanceof NumericAttributeDefinitionFormObject ) {
				numericAttributePrecisions = ((NumericAttributeDefinitionFormObject<?>) formObject).getPrecisions();
				tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
			}
		} else {
			attributeDefaults = null;
			numericAttributePrecisions = null;
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
	@NotifyChange("attributeDefaults")
	public void deleteAttributeDefault(@BindingParam("attributeDefault") AttributeDefault attributeDefault) {
		attributeDefaults.remove(attributeDefault);
	}
	
	@Command
	@NotifyChange("numericAttributePrecisions")
	public void addNumericAttributePrecision() {
		if ( numericAttributePrecisions == null ) {
			initNumericAttributePrecisionsList();
		}
		Precision precision = new Precision();
		numericAttributePrecisions.add(precision);
	}
	
	@Command
	@NotifyChange("numericAttributePrecisions")
	public void deleteNumericAttributePrecision(@BindingParam("precision") Precision precision) {
		numericAttributePrecisions.remove(precision);
	}

	protected void initAttributeDefaultsList() {
		if ( attributeDefaults == null ) {
			attributeDefaults = new ArrayList<AttributeDefault>();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			((AttributeDefinitionFormObject<?>) formObject).setAttributeDefaults(attributeDefaults);
		}
	}
	
	protected void initNumericAttributePrecisionsList() {
		if ( numericAttributePrecisions == null ) {
			numericAttributePrecisions = new ArrayList<Precision>();
			tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
			((NumericAttributeDefinitionFormObject<?>) formObject).setPrecisions(numericAttributePrecisions);
		}
	}
	
	protected void validateForm() {
		if ( editedItem != null ) {
			Binder binder = (Binder) attributeDetailsInclude.getAttribute("$BINDER$");
			validateForm(binder);
		}
	}
		
	protected void validateForm(@ContextParam(ContextType.BINDER) Binder binder) {
		Component view = binder.getView();
		IdSpace currentIdSpace = view.getSpaceOwner();
		Component formComponent = Path.getComponent(currentIdSpace, "nodeFormContainer");
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
	
	public List<Precision> getNumericAttributePrecisions() {
		return numericAttributePrecisions;
	}
	
	public Form getTempFormObject() {
		return tempFormObject;
	}

}
