/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.form.NumericAttributeDefinitionFormObject;
import org.openforis.collect.designer.form.PrecisionFormObject;
import org.openforis.collect.designer.util.Resources;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class NumericAttributeVM extends AttributeVM<NumericAttributeDefinition> {

	private static final String NUMBER_ATTRIBUTE_PRECISIONS_FIELD = "precisions";

	protected List<PrecisionFormObject> numericAttributePrecisions;
	protected PrecisionFormObject selectedPrecision;
	
	//popups
	private Window unitsPopUp;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") NumericAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}
	
	
	@Override
	public void setEditedItem(NumericAttributeDefinition editedItem) {
		super.setEditedItem(editedItem);
		if ( editedItem != null ) {
			numericAttributePrecisions = ((NumericAttributeDefinitionFormObject<?>) formObject).getPrecisions();
			tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
		} else {
			numericAttributePrecisions = null;
		}
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

	@GlobalCommand
	public void openUnitsManagerPopUp() {
		if ( unitsPopUp == null ) {
			dispatchCurrentFormValidatedCommand(true);
			unitsPopUp = openPopUp(Resources.Component.UNITS_MANAGER_POP_UP.getLocation(), true);
		}
	}
	
	@GlobalCommand
	public void closeUnitsManagerPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( unitsPopUp != null && checkCurrentFormValid() ) {
			closePopUp(unitsPopUp);
			unitsPopUp = null;
			validateForm(binder);
		}
	}
	
	protected void initNumericAttributePrecisionsList() {
		if ( numericAttributePrecisions == null ) {
			numericAttributePrecisions = new ArrayList<PrecisionFormObject>();
			tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
			((NumericAttributeDefinitionFormObject<?>) formObject).setPrecisions(numericAttributePrecisions);
		}
	}
	
	public List<PrecisionFormObject> getNumericAttributePrecisions() {
		return numericAttributePrecisions;
	}
	
	public PrecisionFormObject getSelectedPrecision() {
		return selectedPrecision;
	}

	public void setSelectedPrecision(PrecisionFormObject selectedPrecision) {
		this.selectedPrecision = selectedPrecision;
	}
	
}
