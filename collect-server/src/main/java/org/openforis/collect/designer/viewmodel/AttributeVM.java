package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.form.AttributeDefinitionFormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.NumericAttributeDefinitionFormObject;
import org.openforis.collect.designer.form.PrecisionFormObject;
import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
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
public abstract class AttributeVM<T extends AttributeDefinition> extends SurveyObjectBaseVM<T> {

	private static final String FORM_CONTAINER_ID = "nodeFormContainer";
	private static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	private static final String NUMBER_ATTRIBUTE_PRECISIONS_FIELD = "precisions";
	
	protected Form tempFormObject;
	protected List<AttributeDefault> attributeDefaults;
	protected List<PrecisionFormObject> numericAttributePrecisions;
	protected AttributeDefault selectedAttributeDefault;
	protected PrecisionFormObject selectedPrecision;
	
	@Wire
	private Include attributeDetailsInclude;
	
	protected void afterCompose(Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
		 refreshNodeForm();
	}
	
	@Init
	public void init(@ExecutionArgParam("item") T attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		if ( attributeDefn != null ) {
			this.newItem = newItem;
			setEditedItem(attributeDefn);
		}
	}
	
	protected void refreshNodeForm() {
		String type = getAttributeType();
		attributeDetailsInclude.setSrc("survey_edit/schema/attribute_" + type + ".zul");
		validateForm();
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
	
	@Command
	@NotifyChange("numericAttributePrecisions")
	public void addNumericAttributePrecision() {
		if ( numericAttributePrecisions == null ) {
			initNumericAttributePrecisionsList();
		}
		PrecisionFormObject precision = new PrecisionFormObject();
		numericAttributePrecisions.add(precision);
	}
	
	@Command
	@NotifyChange({"selectedPrecision","numericAttributePrecisions"})
	public void deleteNumericAttributePrecision() {
		numericAttributePrecisions.remove(selectedPrecision);
		selectedPrecision = null;
	}
	
	@Command
	@NotifyChange("selectedPrecision")
	public void selectPrecision(@BindingParam("precision") PrecisionFormObject precision) {
		selectedPrecision = precision;
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
			numericAttributePrecisions = new ArrayList<PrecisionFormObject>();
			tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
			((NumericAttributeDefinitionFormObject<?>) formObject).setPrecisions(numericAttributePrecisions);
		}
	}
	
	protected void validateForm() {
		if ( editedItem != null ) {
			Component formContainer = attributeDetailsInclude.getFellow(FORM_CONTAINER_ID);
			Binder binder = (Binder) formContainer.getAttribute("$BINDER$");
			validateForm(binder);
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
	
	public List<PrecisionFormObject> getNumericAttributePrecisions() {
		return numericAttributePrecisions;
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

	public PrecisionFormObject getSelectedPrecision() {
		return selectedPrecision;
	}

	public void setSelectedPrecision(PrecisionFormObject selectedPrecision) {
		this.selectedPrecision = selectedPrecision;
	}

}
