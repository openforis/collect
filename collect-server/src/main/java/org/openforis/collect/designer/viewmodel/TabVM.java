/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.form.TabFormObject;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

/**
 * @author S. Ricci
 *
 */
public class TabVM extends SurveyObjectBaseVM<UITab> {
	
	protected static final String FORM_CONTAINER_ID = "nodeFormContainer";
	
	private EntityDefinition parentEntity;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") UITab tab, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init();
		if ( tab != null ) {
			this.parentEntity = parentEntity;
			this.newItem = newItem;
			setEditedItem(tab);
		}
	}
	
	@Override
	protected SurveyObjectFormObject<UITab> createFormObject() {
		TabFormObject formObject = TabFormObject.newInstance();
		return formObject;
	}

	@Override
	protected List<UITab> getItemsInternal() {
		return null;
	}

	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {
	}

	@Override
	protected UITab createItemInstance() {
		return null;
	}

	@Override
	@NotifyChange("items")
	protected void addNewItemToSurvey() {
		
	}

	@Override
	protected void deleteItemFromSurvey(UITab item) {
	}
	
	@Override
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		super.commitChanges(binder);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("parentEntity", parentEntity);
		args.put("node", editedItem);
		args.put("newItem", newItem);
		BindUtils.postGlobalCommand(null, null, "editedNodeChanged", args);
	}
	
	@Override
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		setEditedItem(editedItem);
		notifyChange("tempFormObject","formObject");
	}
	
	@Command
	public void labelChanged(@ContextParam(ContextType.BINDER) Binder binder,
			@BindingParam("label") String label) {
		dispatchApplyChangesCommand(binder);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("item", editedItem);
		args.put("name", label);
		BindUtils.postGlobalCommand(null, null, "editedNodeNameChanging", args);
	}
	
}
