/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.CalculatedAttributeDefinitionFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmParams;
import org.openforis.collect.designer.util.Resources;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Formula;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 * 
 */
public class CalculatedAttributeVM extends
		AttributeVM<CalculatedAttributeDefinition> {

	private static final String FORMULAS_FIELD = "formulas";
	public static final String FORMULA_POPUP_PARENT_ENTITY_DEFINITION_ARG = "parentEntityDefinition";
	public static final String FORMULA_POPUP_ATTRIBUTE_DEFINITION_ARG = "attributeDefinition";

	protected List<Formula> formulas;
	private Boolean editingNewFormula;
	private Formula editedFormula;
	protected Formula selectedFormula;

	private Window formulaPopUp;

	@Init(superclass = false)
	public void init(
			@ExecutionArgParam("parentEntity") EntityDefinition parentEntity,
			@ExecutionArgParam("item") CalculatedAttributeDefinition attributeDefn,
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}

	@Override
	@NotifyChange({"editedItem","formObject","tempFormObject"})
	public void setEditedItem(CalculatedAttributeDefinition editedItem) {
		super.setEditedItem(editedItem);
		initFormulas();
	}

	protected void initFormulas() {
		if (editedItem != null) {
			formulas = new ArrayList<Formula>(editedItem.getFormulas());
			updateFormObjectFormulas();
		} else {
			formulas = null;
		}
		notifyChange("formObject", "tempFormObject", "formulas");
	}

	protected void updateFormObjectFormulas() {
		tempFormObject.setField(FORMULAS_FIELD, formulas);
		((CalculatedAttributeDefinitionFormObject<?>) formObject)
				.setFormulas(formulas);
	}

	@Command
	@NotifyChange("formulas")
	public void addFormula() {
		editingNewFormula = true;
		editedFormula = new Formula();
		openFormulaEditPopUp();
	}

	@Command
	public void editFormula() {
		editingNewFormula = false;
		editedFormula = selectedFormula;
		openFormulaEditPopUp();
	}

	@Command
	@NotifyChange({ "selectedFormula", "formulas" })
	public void deleteFormula() {
		ConfirmParams params = new MessageUtil.ConfirmParams(
				new MessageUtil.ConfirmHandler() {
					@Override
					public void onOk() {
						editedItem.removeFormula(selectedFormula);
						selectedFormula = null;
						initFormulas();
						notifyChange("selectedFormula", "formulas");
					}
				}, "survey.schema.attribute.calculated.formula.confirm_delete");
		params.setOkLabelKey("global.delete_item");
		MessageUtil.showConfirm(params);
	}

	@Command
	@NotifyChange("selectedFormula")
	public void selectFormula(@BindingParam("formula") Formula formula) {
		selectedFormula = formula;
	}

	protected void openFormulaEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(FORMULA_POPUP_PARENT_ENTITY_DEFINITION_ARG, parentEntity );
		args.put(FORMULA_POPUP_ATTRIBUTE_DEFINITION_ARG, editedItem);
		args.put("newItem", editingNewFormula);
		args.put("formula", editedFormula);
		formulaPopUp = openPopUp(
				Resources.Component.FORMULA_POPUP.getLocation(),
				true, args);
	}

	@GlobalCommand
	public void applyChangesToEditedFormula(
			@ContextParam(ContextType.BINDER) Binder binder) {
		if (editedFormula != null && checkCanLeaveForm()) {
			closeFormulaEditPopUp(binder);
			editedFormula = null;
			initFormulas();
			notifyChange("formulas");
		}
	}

	@GlobalCommand
	public void cancelChangesToEditedFormula(
			@ContextParam(ContextType.BINDER) Binder binder) {
		// TODO confirm if there are not committed changes
		if (editedFormula != null) {
			closeFormulaEditPopUp(binder);
			editedFormula = null;
		}
	}

	protected void closeFormulaEditPopUp(Binder binder) {
		closePopUp(formulaPopUp);
		formulaPopUp = null;
		validateForm(binder);
	}

	@Command
	@NotifyChange({ "formulas" })
	public void moveSelectedFormulaUp() {
		moveSelectedFormula(true);
	}

	@Command
	@NotifyChange({ "formulas" })
	public void moveSelectedFormulaDown() {
		moveSelectedFormula(false);
	}

	protected void moveSelectedFormula(boolean up) {
		int indexFrom = getSelectedFormulaIndex();
		int indexTo = up ? indexFrom - 1 : indexFrom + 1;
		moveSelectedFormula(indexTo);
	}

	protected int getSelectedFormulaIndex() {
		List<?> items = editedItem.getFormulas();
		int index = items.indexOf(selectedFormula);
		return index;
	}

	protected void moveSelectedFormula(int indexTo) {
		editedItem.moveFormula(selectedFormula, indexTo);
		initFormulas();
	}

	@DependsOn({ "formulas", "selectedFormula" })
	public boolean isMoveSelectedFormulaUpDisabled() {
		return isMoveSelectedFormulaDisabled(true);
	}

	@DependsOn({ "formulas", "selectedFormula" })
	public boolean isMoveSelectedFormulaDownDisabled() {
		return isMoveSelectedFormulaDisabled(false);
	}

	protected boolean isMoveSelectedFormulaDisabled(boolean up) {
		if (selectedFormula == null) {
			return true;
		} else {
			List<Formula> siblings = editedItem.getFormulas();
			int index = siblings.indexOf(selectedFormula);
			return up ? index <= 0 : index < 0 || index >= siblings.size() - 1;
		}
	}

	public List<Formula> getFormulas() {
		return formulas;
	}

	public Form getTempFormObject() {
		return tempFormObject;
	}

	public Formula getSelectedFormula() {
		return selectedFormula;
	}

	public void setSelectedFormula(Formula selectedFormula) {
		this.selectedFormula = selectedFormula;
	}

}
