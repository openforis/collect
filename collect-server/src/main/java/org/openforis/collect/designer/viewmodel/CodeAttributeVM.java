/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.CodeAttributeDefinitionFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmParams;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.designer.viewmodel.SchemaTreePopUpVM.NodeSelectedEvent;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Window;

import liquibase.util.StringUtils;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeVM extends AttributeVM<CodeAttributeDefinition> {

	private static final String CODE_LIST_ASSIGNED_COMMAND = "codeListAssigned";
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") CodeAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.initInternal(parentEntity, attributeDefn, newItem);
	}

	@Command
	public void onListChanged(@ContextParam(ContextType.BINDER) Binder binder,
			@BindingParam("list") CodeList list) {
		CodeAttributeDefinitionFormObject fo = (CodeAttributeDefinitionFormObject) getFormObject();
		CodeList oldList = fo.getList();
		boolean listChanged = oldList != null && ! oldList.equals(list);
		if (oldList == null) {
			performListChange(binder, list);
		} else if (listChanged) {
			if (editedItem.hasDependentCodeAttributeDefinitions() ) {
				confirmParentCodeListChange(binder, list);
			} else {
				performListChange(binder, list);
			}
		}
	}

	private void confirmCodeListChange(final Binder binder, final CodeList list) {
		CodeList oldList = ((CodeAttributeDefinitionFormObject) getFormObject()).getList();
		
		ConfirmParams confirmParams = new ConfirmParams(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				performListChange(binder, list);
			}
		}, "survey.schema.attribute.code.confirm_change_list.message");
		confirmParams.setOkLabelKey("global.change");
		confirmParams.setCancelLabelKey("global.leave_original_value");
		confirmParams.setMessageArgs(new String[] {oldList.getName(), list.getName()});
		MessageUtil.showConfirm(confirmParams);
	}

	private void confirmParentCodeListChange(final Binder binder, final CodeList list) {
		ConfirmParams confirmParams = new ConfirmParams(new MessageUtil.CompleteConfirmHandler() {
			@Override
			public void onOk() {
				performListChange(binder, list);
			}
			@Override
			public void onCancel() {
				CodeList oldList = editedItem.getList();
				setFormFieldValue(binder, "list", oldList);
				setTempFormObjectFieldValue("list", oldList);
			}
		}, "survey.schema.attribute.code.confirm_change_list_on_referenced_node.message");
		confirmParams.setOkLabelKey("survey.schema.attribute.code.confirm_change_list_on_referenced_node.ok");
		confirmParams.setTitleKey("survey.schema.attribute.code.confirm_change_list_on_referenced_node.title");
		List<String> dependentAttributePaths = new ArrayList<String>();
		for (CodeAttributeDefinition codeAttributeDefinition : editedItem.getDependentCodeAttributeDefinitions()) {
			dependentAttributePaths.add(codeAttributeDefinition.getPath());
		}
		confirmParams.setMessageArgs(new String[]{StringUtils.join(dependentAttributePaths, ", ")});
		MessageUtil.showConfirm(confirmParams);
	}
	
	private void performListChange(Binder binder, CodeList list) {
		CodeAttributeDefinitionFormObject fo = (CodeAttributeDefinitionFormObject) getFormObject();
		CodeList oldList = fo.getList();
		fo.setParentCodeAttributeDefinition(null);
		fo.setList(list);
		setFormFieldValue(binder, "list", list);
		setFormFieldValue(binder, "list.hierarchical", list != null && list.isHierarchical());
		setFormFieldValue(binder, "parentCodeAttributeDefinitionPath", null);
		setFormFieldValue(binder, "hierarchicalLevel", null);
		dispatchApplyChangesCommand(binder);
		notifyChange("dependentCodePaths");
		
		dispatchCodeListAssignedCommand(list, oldList);
	}

	private void dispatchCodeListAssignedCommand(CodeList list, CodeList oldList) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("list", list);
		args.put("oldList", oldList);
		args.put("codeAttribute", editedItem);
		BindUtils.postGlobalCommand(null, null, CODE_LIST_ASSIGNED_COMMAND, args);
	}

	@GlobalCommand
	public void codeListsPopUpClosed(@ContextParam(ContextType.BINDER) Binder binder, 
			@BindingParam(CodeListsVM.EDITING_ATTRIBUTE_PARAM) Boolean editingAttribute, 
			@BindingParam(CodeListsVM.SELECTED_CODE_LIST_PARAM) CodeList selectedCodeList) {
		if ( editingAttribute && selectedCodeList != null ) {
			CodeAttributeDefinitionFormObject fo = (CodeAttributeDefinitionFormObject) getFormObject();
			CodeList oldList = fo.getList();
			if (oldList != null && ! oldList.equals(selectedCodeList)) {
				if (oldList != survey.getSamplingDesignCodeList()) {
					confirmCodeListChange(binder, selectedCodeList);
				}
			} else {
				onListChanged(binder, selectedCodeList);
				validateForm(binder);
			}
		}
	}

	@Command
	public void openParentAttributeSelector(@ContextParam(ContextType.BINDER) final Binder binder) {
		String title = Labels.getLabel("survey.schema.attribute.code.select_parent_for_node", new String[]{editedItem.getName()});

		final Collection<CodeAttributeDefinition> assignableParentAttributes = editedItem.getAssignableParentCodeAttributeDefinitions();
		if ( assignableParentAttributes.isEmpty() ) {
			MessageUtil.showWarning("survey.schema.attribute.code.no_assignable_parent_available");
		} else {
			CodeAttributeDefinition parentCodeAttributeDefinition = ((CodeAttributeDefinitionFormObject) formObject).getParentCodeAttributeDefinition();
			Predicate<SurveyObject> includedNodePredicate = new Predicate<SurveyObject>() {
				@Override
				public boolean evaluate(SurveyObject item) {
					return item instanceof UITab || item instanceof EntityDefinition ||
							item instanceof CodeAttributeDefinition && assignableParentAttributes.contains(item);
				}
			};
			Predicate<SurveyObject> disabledNodePredicate = new Predicate<SurveyObject>() {
				@Override
				public boolean evaluate(SurveyObject item) {
					return ! (item instanceof CodeAttributeDefinition);
				}
			};
			final Window parentSelectorPopUp = SchemaTreePopUpVM.openPopup(title,
					editedItem.getRootEntity(), null, includedNodePredicate,
					false, false, disabledNodePredicate, null,
					parentCodeAttributeDefinition, true);
			parentSelectorPopUp.addEventListener(SchemaTreePopUpVM.NODE_SELECTED_EVENT_NAME, new EventListener<NodeSelectedEvent>() {
				public void onEvent(NodeSelectedEvent event) throws Exception {
					CodeAttributeDefinition parentAttrDefn = (CodeAttributeDefinition) event.getSelectedItem();
					CodeAttributeDefinitionFormObject fo = (CodeAttributeDefinitionFormObject) formObject;
					fo.setParentCodeAttributeDefinition(parentAttrDefn);
					String hierarchicalLevel = getHierarchicalLevelName(parentAttrDefn);
					fo.setHierarchicalLevel(hierarchicalLevel);
					notifyChange("formObject");
					dispatchApplyChangesCommand(binder);
					notifyChange("dependentCodePaths");
					closePopUp(parentSelectorPopUp);
				}
			});
		}
	}

	private String getHierarchicalLevelName(CodeAttributeDefinition parentAttrDefn) {
		if (parentAttrDefn == null) {
			return null;
		} else {
			Integer parentLevelIndex = parentAttrDefn.getListLevelIndex();
			int levelIndex = parentLevelIndex + 1;
			CodeListLevel level = parentAttrDefn.getList().getHierarchy().get(levelIndex);
			return level.getName();
		}
	}

	@Command
	public void layoutTypeChange(@ContextParam(ContextType.BINDER) Binder binder, 
			@BindingParam("layoutType") String layoutType) {
		setTempFormObjectFieldValue("showAllowedValuesPreview", Annotation.SHOW_ALLOWED_VALUES_PREVIEW.getDefaultValue());
		String layoutDirection = null;
		if ( "radio".equals(layoutType) ) {
			layoutDirection = Annotation.CODE_ATTRIBUTE_LAYOUT_DIRECTION.getDefaultValue();
		}
		setTempFormObjectFieldValue("layoutDirection", layoutDirection);
		dispatchApplyChangesCommand(binder);
	}
	
	public String getDependentCodePaths() {
		if ( newItem ) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder();
			Collection<CodeAttributeDefinition> dependents = editedItem.getDependentCodeAttributeDefinitions();
			Iterator<CodeAttributeDefinition> it = dependents.iterator();
			while (it.hasNext()) {
				CodeAttributeDefinition dependent = it.next();
				sb.append(dependent.getPath());
				if ( it.hasNext() ) {
					sb.append(", ");
				}
			}
			return sb.toString();
		}
	}

}
