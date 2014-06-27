/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Path;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class NodeDefinitionVM<T extends NodeDefinition> extends SurveyObjectBaseVM<T> {

	protected static final String FORM_CONTAINER_ID = "nodeFormContainer";
	
	protected Form tempFormObject;
	protected EntityDefinition parentEntity;

	@Init(superclass=false)
	public void init(EntityDefinition parentEntity, T nodeDefn, Boolean newItem) {
		super.init();
		if ( nodeDefn != null ) {
			this.parentEntity = parentEntity;
			this.newItem = newItem;
			setEditedItem(nodeDefn);
		}
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
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		setEditedItem(editedItem);
		notifyChange("tempFormObject","formObject");
	}
	
	@Override
	protected void commitChanges() {
		formObject.saveTo(editedItem, currentLanguageCode);
		boolean editingRootEntity = parentEntity == null;
		boolean wasNewItem = newItem;
		if ( wasNewItem ) {
			if ( editingRootEntity ) {
//				CollectSurvey survey = getSurvey();
//				UIOptions uiOptions = survey.getUIOptions();
//				uiOptions.createRootTabSet((EntityDefinition) editedItem);
//				Schema schema = editedItem.getSchema();
//				schema.addRootEntityDefinition((EntityDefinition) editedItem);
			} else {
				parentEntity.addChildDefinition(editedItem);
			}
			newItem = false;
		}
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("parentEntity", parentEntity);
		args.put("node", editedItem);
		args.put("newItem", wasNewItem);
		BindUtils.postGlobalCommand(null, null, "editedNodeChanged", args);
		dispatchSurveyChangedCommand();
	}

	@Command
	public void nameChanged(@ContextParam(ContextType.BINDER) Binder binder,
			@BindingParam("name") String name) {
		dispatchApplyChangesCommand(binder);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("item", editedItem);
		args.put("name", name);
		BindUtils.postGlobalCommand(null, null, "editedNodeNameChanging", args);
	}
	
	protected String getInstanceLabel(NodeDefinition nodeDefn) {
		String label = nodeDefn.getLabel(Type.INSTANCE, currentLanguageCode);
		return label;
	}
	
	@Override
	protected List<T> getItemsInternal() {
		return null;
	}

	@Override
	protected void moveSelectedItem(int indexTo) {
		//do nothing
	}

	@Override
	protected void deleteItemFromSurvey(T item) {
		//do nothing
	}
	
	@GlobalCommand
	public void tabSetChanged(@ContextParam(ContextType.BINDER) Binder binder, @BindingParam("tabSet") UITabSet tabSet) {
		notifyChange("assignableTabNames");
		if ( isEditingItem() ) {
			validateForm(binder);
		}
	}
	
	@GlobalCommand
	public void tabChanged(@ContextParam(ContextType.BINDER) Binder binder, @BindingParam("tab") UITab tab ) {
		notifyChange("assignableTabNames");
	}
	
	@GlobalCommand
	public void closeVersioningManagerPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		validateForm(binder);
	}
	
	protected void validateForm(@ContextParam(ContextType.BINDER) Binder binder) {
		Component view = binder.getView();
		IdSpace currentIdSpace = view.getSpaceOwner();
		Component formComponent = Path.getComponent(currentIdSpace, FORM_CONTAINER_ID);
		Binder formComponentBinder = (Binder) formComponent.getAttribute("binder");
		formComponentBinder.postCommand("applyChanges", null);
	}
	
	// GETTERS AND SETTERS
	public Form getTempFormObject() {
		return tempFormObject;
	}
	
	@DependsOn("editedItem")
	public String getNodeType() {
		if ( editedItem != null ) {
			NodeType type = NodeType.valueOf(editedItem);
			return type.name();
		} else {
			return null;
		}
	}

	@DependsOn("editedItem")
	public String getAttributeType() {
		if ( editedItem != null && editedItem instanceof AttributeDefinition ) {
			AttributeType type = AttributeType.valueOf((AttributeDefinition) editedItem);
			return type.name();
		} else {
			return null;
		}
	}
	
	@DependsOn("editedItem")
	public String getAttributeTypeLabel() {
		String type = getAttributeType();
		return getAttributeTypeLabel(type);
	}

	public String getAttributeTypeLabel(String typeValue) {
		if ( StringUtils.isNotBlank(typeValue) ) {
			AttributeType type = AttributeType.valueOf(typeValue);
			return type.getLabel();
		} else {
			return null;
		}
	}
	
	@DependsOn("editedItem")
	public boolean isRootEntity() {
		return editedItem != null && parentEntity == null;
	}
	
	public boolean isRequiredApplied() {
		return true;
	}
	
	public boolean isCalculatedAttribute() {
		return editedItem != null && editedItem instanceof CalculatedAttributeDefinition;
	}
}
