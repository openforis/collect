/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.CalculatedAttributeFormulaFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Formula;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class CalculatedAttributeFormulaVM extends SurveyObjectBaseVM<Formula> {

	private static final String APPLY_CHANGES_TO_EDITED_FORMULA_GLOBAL_COMMAND = "applyChangesToEditedFormula";
	
	protected CalculatedAttributeDefinition attributeDefinition;

	public CalculatedAttributeFormulaVM() {
		setCommitChangesOnApply(false);
	}
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam(CalculatedAttributeVM.FORMULA_POPUP_ATTRIBUTE_DEFINITION_ARG) CalculatedAttributeDefinition attrDefn,
			@ExecutionArgParam("formula") Formula formula, @ExecutionArgParam("newItem") Boolean newItem) {
		super.init();
		this.attributeDefinition = attrDefn;
		this.newItem = newItem;
		setEditedItem(formula);
	}

	@Override
	protected FormObject<Formula> createFormObject() {
		return new CalculatedAttributeFormulaFormObject();
	}

	@Override
	protected List<Formula> getItemsInternal() {
		return null;
	}

	@Override
	protected void moveSelectedItem(int indexTo) {
	}

	@Override
	protected Formula createItemInstance() {
		return null;
	}

	@Override
	protected void addNewItemToSurvey() {
		attributeDefinition.addFormula(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(Formula item) {
	}
	
	@Command
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		dispatchApplyChangesCommand(binder);
		if ( checkCanLeaveForm() ) {
			super.commitChanges();
			BindUtils.postGlobalCommand(null, null, APPLY_CHANGES_TO_EDITED_FORMULA_GLOBAL_COMMAND, null);
		}
	}
	
}
