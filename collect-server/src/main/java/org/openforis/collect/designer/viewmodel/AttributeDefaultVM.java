/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.Arrays;
import java.util.List;

import org.openforis.collect.designer.form.AttributeDefaultFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class AttributeDefaultVM extends SurveyObjectPopUpVM<AttributeDefault> {

	private static final String APPLY_CHANGES_TO_EDITED_ATTRIBUTE_DEFAULT_GLOBAL_COMMAND = "applyChangesToEditedAttributeDefault";
	private static final String CANCEL_CHANGES_TO_EDITED_ATTRIBUTE_DEFAULT_GLOBAL_COMMAND = "cancelChangesToEditedAttributeDefault";
	
	protected AttributeDefinition parentDefinition;

	public AttributeDefaultVM() {
		setCommitChangesOnApply(false);
		fieldLabelKeyPrefixes.addAll(0, Arrays.asList("survey.schema.attribute.attribute_default"));
	}
	
	@Init(superclass=false)
	public void init(@ContextParam(ContextType.BINDER) Binder binder, @ExecutionArgParam("parentDefinition") AttributeDefinition parentDefinition,
			@ExecutionArgParam("attributeDefault") AttributeDefault attributeDefault, @ExecutionArgParam("newItem") Boolean newItem) {
		super.init();
		this.parentDefinition = parentDefinition;
		this.newItem = newItem;
		setEditedItem(attributeDefault);
		if (attributeDefault != null && ! newItem) {
			validateForm(binder);
		}
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
	
	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
	}
	
	@Override
	protected void dispatchChangesAppliedCommand(boolean ignoreUnsavedChanges) {
		BindUtils.postGlobalCommand(null, null, APPLY_CHANGES_TO_EDITED_ATTRIBUTE_DEFAULT_GLOBAL_COMMAND, null);		
	}
	
	@Override
	protected void dispatchChangesCancelledCommand() {
		BindUtils.postGlobalCommand(null, null, CANCEL_CHANGES_TO_EDITED_ATTRIBUTE_DEFAULT_GLOBAL_COMMAND, null);
	}

}
