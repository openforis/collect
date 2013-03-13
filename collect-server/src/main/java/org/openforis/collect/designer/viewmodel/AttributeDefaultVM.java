/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.AttributeDefaultFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
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
public class AttributeDefaultVM extends SurveyObjectBaseVM<AttributeDefault> {

	private static final String APPLY_CHANGES_TO_EDITED_ATTRIBUTE_DEFAULT_GLOBAL_COMMAND = "applyChangesToEditedAttributeDefault";
	
	protected AttributeDefinition parentDefinition;

	public AttributeDefaultVM() {
		setCommitChangesOnApply(false);
	}
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentDefinition") AttributeDefinition parentDefinition,
			@ExecutionArgParam("attributeDefault") AttributeDefault attributeDefault, @ExecutionArgParam("newItem") Boolean newItem) {
		super.init();
		this.parentDefinition = parentDefinition;
		this.newItem = newItem;
		setEditedItem(attributeDefault);
	}

	@Override
	protected FormObject<AttributeDefault> createFormObject() {
		return new AttributeDefaultFormObject();
	}

	@Override
	protected List<AttributeDefault> getItemsInternal() {
		return null;
	}

	@Override
	protected void moveSelectedItem(int indexTo) {
	}

	@Override
	protected AttributeDefault createItemInstance() {
		return null;
	}

	@Override
	protected void addNewItemToSurvey() {
		parentDefinition.addAttributeDefault(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(AttributeDefault item) {
	}
	
	@Command
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		dispatchApplyChangesCommand(binder);
		if ( checkCanLeaveForm() ) {
			super.commitChanges();
			BindUtils.postGlobalCommand(null, null, APPLY_CHANGES_TO_EDITED_ATTRIBUTE_DEFAULT_GLOBAL_COMMAND, null);
		}
	}
	
}
