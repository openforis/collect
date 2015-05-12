/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.CalculatedAttributeFormulaFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.Calculable;
import org.openforis.idm.metamodel.Formula;
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
	
	public static final String PARENT_ENTITY_DEFINITION_ARG = "parentEntityDefinition";
	public static final String ATTRIBUTE_DEFINITION_ARG = "attributeDefinition";

	protected AttributeDefinition attributeDefinition;

	public CalculatedAttributeFormulaVM() {
		setCommitChangesOnApply(false);
	}
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam(ATTRIBUTE_DEFINITION_ARG) AttributeDefinition attrDefn,
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
		((Calculable) attributeDefinition).addFormula(editedItem);
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
