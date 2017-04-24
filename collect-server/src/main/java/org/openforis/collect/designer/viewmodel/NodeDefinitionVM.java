/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.designer.metamodel.NodeType;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.utils.SurveyObjects;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
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
	private static final String NAME_FIELD_NAME = "name";
	private static final String INSTANCE_LABEL_FIELD_NAME = "instanceLabel";

	protected Form tempFormObject;
	protected EntityDefinition parentEntity;

	public NodeDefinitionVM() {
		super();
		fieldLabelKeyPrefixes.addAll(Arrays.asList("survey.schema.node"));
	}
	
	protected void initInternal(EntityDefinition parentEntity, T nodeDefn, Boolean newItem) {
		super.init();
		tempFormObject = new SimpleForm();
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
	protected void moveSelectedItemInSurvey(int indexTo) {
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
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
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
				schemaUpdater.addChildDefinition(parentEntity, editedItem);
				validateForm(binder);
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
		name = SurveyObjects.adjustInternalName(name);
		setTempFormObjectFieldValue(NAME_FIELD_NAME, name);
		((NodeDefinitionFormObject<?>) formObject).setName(name);

		//suggest label
		String singleInstanceLabel = getTempFormObjectFieldValue(INSTANCE_LABEL_FIELD_NAME);
		if (StringUtils.isBlank(singleInstanceLabel) && StringUtils.isNotBlank(name)) {
			singleInstanceLabel = suggestLabel(name);
			setTempFormObjectFieldValue(INSTANCE_LABEL_FIELD_NAME, singleInstanceLabel);
			((NodeDefinitionFormObject<?>) formObject).setInstanceLabel(singleInstanceLabel);
		}
		dispatchApplyChangesCommand(binder);
		
		//notify name change
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("item", editedItem);
		args.put("name", name);
		BindUtils.postGlobalCommand(null, null, "editedNodeNameChanging", args);
	}

	@Command
	public void singleInstanceLabelChange(@ContextParam(ContextType.BINDER) Binder binder,
			@BindingParam("label") String value) {
		dispatchApplyChangesCommand(binder);
		
		((NodeDefinitionFormObject<?>) formObject).setInstanceLabel(value);
		String name = getTempFormObjectFieldValue(NAME_FIELD_NAME);
		if ( StringUtils.isBlank(name) && StringUtils.isNotBlank(value) ) {
			name = suggestInternalName(value);
			nameChanged(binder, name);
		}
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
	
	protected <V> V getTempFormObjectFieldValue(String field) {
		return getFormFieldValue(tempFormObject, field);
	}
	
	protected void setTempFormObjectFieldValue(String field, Object value) {
		setFormFieldValue(tempFormObject, field, value);
	}
	
	@DependsOn("editedItem")
	public String getNodeType() {
		if ( editedItem == null ) {
			return null;
		} else {
			NodeType type = NodeType.valueOf(editedItem);
			return type.name();
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
	
	@DependsOn("editedItem")
	public boolean isParentEntityRoot() {
		return editedItem != null && parentEntity != null && 
				(parentEntity.isRoot() 
				|| 
				(! parentEntity.isMultiple() && parentEntity.getNearestAncestorMultipleEntity().isRoot())
		);
	}
	
	@DependsOn("editedItem")
	public boolean isAttribute() {
		return editedItem != null && editedItem instanceof AttributeDefinition;
	}
	
	@DependsOn("editedItem")
	public boolean isInsideTableEntity() {
		UIOptions uiOptions = getSurvey().getUIOptions();
		return editedItem != null && parentEntity != null && parentEntity.isMultiple() && 
				uiOptions.getLayout(parentEntity) == Layout.TABLE; 
	}
	
	@DependsOn("editedItem")
	public boolean isMultipleFieldEditingDisabled() {
//		return editedItem == null || ! (editedItem instanceof CodeAttributeDefinition);
		return false;
	}
	
	public boolean isRequiredApplied() {
		return true;
	}
	
	public List<Map<String, String>> getDependentNodes() {
		List<Map<String, String>> result = new ArrayList<Map<String,String>>();
		try {
			result.addAll(getDependentNodeInfos("relevancy", editedItem.getRelevancyDependentDefinitions()));
			result.addAll(getDependentNodeInfos("min_count", editedItem.getMinCountDependentDefinitions()));
			result.addAll(getDependentNodeInfos("max_count", editedItem.getMaxCountDependentDefinitions()));
			result.addAll(getDependentNodeInfos("calculated_value", editedItem.getCalculatedValueDependentDefinitions()));
		} catch(Exception e) {
			//ignore
		}
		return result;
	}
	
	public boolean isPredefinedCollectEarthAttribute() {
		return editedItem != null && ! newItem && editedItem.getParentEntityDefinition() != null && editedItem.getParentEntityDefinition().isRoot()
				&& CollectEarthSurveyValidator.REQUIRED_FIELD_NAMES.contains(editedItem.getName());
	}

	protected List<Map<String, String>> getDependentNodeInfos(String type, List<NodeDefinition> nodes) {
		List<Map<String, String>> result = new ArrayList<Map<String,String>>();
		for (NodeDefinition node : nodes) {
			Map<String, String> nodeInfo = new HashMap<String, String>();
			nodeInfo.put("type", type);
			nodeInfo.put("path", node.getPath());
			if ( ! result.contains(nodeInfo) ) {
				result.add(nodeInfo);
			}
		}
		return result;
	}
	
	protected boolean checkNodeAttached() {
		if (editedItem == null || editedItem.getParentDefinition() == null) {
			MessageUtil.showWarning("survey.schema.node.error.node_not_yet_attached");
			return false;
		} else {
			return true;
		}
	}
	
}
