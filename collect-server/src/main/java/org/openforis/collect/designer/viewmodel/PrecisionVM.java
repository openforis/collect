/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.PrecisionFormObject;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Precision;
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
public class PrecisionVM extends SurveyObjectBaseVM<Precision> {

	private static final String APPLY_CHANGES_TO_EDITED_PRECISION_GLOBAL_COMMAND = "applyChangesToEditedPrecision";
	
	protected NumericAttributeDefinition parentDefinition;

	public PrecisionVM() {
		setCommitChangesOnApply(false);
	}
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentDefinition") NumericAttributeDefinition parentDefinition,
			@ExecutionArgParam("precision") Precision precision, @ExecutionArgParam("newItem") Boolean newItem) {
		super.init();
		this.parentDefinition = parentDefinition;
		this.newItem = newItem;
		setEditedItem(precision);
	}

	@Override
	protected FormObject<Precision> createFormObject() {
		return survey == null ? new PrecisionFormObject() : new PrecisionFormObject(survey);
	}

	@Override
	protected List<Precision> getItemsInternal() {
		return null;
	}

	@Override
	protected Precision createItemInstance() {
		return null;
	}

	@Override
	protected void addNewItemToSurvey() {
		parentDefinition.addPrecisionDefinition(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(Precision item) {
		//do nothing
	}

	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
		//do nothing
	}

	@Command
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		dispatchApplyChangesCommand(binder);
		if ( checkCanLeaveForm() ) {
			super.commitChanges(binder);
			BindUtils.postGlobalCommand(null, null, APPLY_CHANGES_TO_EDITED_PRECISION_GLOBAL_COMMAND, null);
		}
	}
	
}
