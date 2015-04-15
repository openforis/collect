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

import liquibase.util.StringUtils;

import org.openforis.collect.designer.form.CodeAttributeDefinitionFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmParams;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeVM extends AttributeVM<CodeAttributeDefinition> {

	private static final String CODE_LIST_ASSIGNED_COMMAND = "codeListAssigned";
	private static final String FORM_ID = "fx";

	private Window parentSelectorPopUp;

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") CodeAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}

	@Command
	public void onListChanged(@ContextParam(ContextType.BINDER) final Binder binder,
			@BindingParam("list") final CodeList list) {
		CodeAttributeDefinitionFormObject fo = (CodeAttributeDefinitionFormObject) getFormObject();
		CodeList oldList = fo.getList();
		boolean listChanged = oldList != null && ! oldList.equals(list);
		if (oldList == null || listChanged) {
			if (listChanged && editedItem.hasDependentCodeAttributeDefinitions() ) {
				confirmParentCodeListChange(binder, list);
			} else {
				performListChange(binder, list);
			}
		}
	}

	private void confirmParentCodeListChange(final Binder binder, final CodeList list) {
		ConfirmParams confirmParams = new ConfirmParams(new MessageUtil.CompleteConfirmHandler() {
			@Override
			public void onOk() {
				performListChange(binder, list);
			}
			@Override
			public void onCancel() {
				Form form = getForm(binder);
				CodeList oldList = editedItem.getList();
				setValueOnFormField(form, "list", oldList);
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
	
	private void performListChange(final Binder binder,
			final CodeList list) {
		CodeAttributeDefinitionFormObject fo = (CodeAttributeDefinitionFormObject) getFormObject();
		CodeList oldList = fo.getList();
		fo.setParentCodeAttributeDefinition(null);
		fo.setList(list);
		Form form = getForm(binder);
		setValueOnFormField(form, "list", list);
		setValueOnFormField(form, "list.hierarchical", list != null && list.isHierarchical());
		setValueOnFormField(form, "parentCodeAttributeDefinition.path", null);
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
		if ( editingAttribute ) {
			if ( selectedCodeList != null ) {
				onListChanged(binder, selectedCodeList);
			}
			validateForm(binder);
		}
	}

	protected Form getForm(Binder binder) {
		Component view = binder.getView();
		return (Form) view.getAttribute(FORM_ID);
	}

	@Command
	public void openParentAttributeSelector() {
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
					return item instanceof UITab;
				}
			};
			parentSelectorPopUp = SchemaTreePopUpVM.openPopup(title,
					editedItem.getRootEntity(), null, includedNodePredicate,
					false, disabledNodePredicate, null,
					parentCodeAttributeDefinition);
		}
	}

	@GlobalCommand
	public void closeSchemaNodeSelector() {
		if ( parentSelectorPopUp != null ) {
			closePopUp(parentSelectorPopUp);
			parentSelectorPopUp = null;
		}
	}
	
	@GlobalCommand
	public void schemaTreeNodeSelected(@ContextParam(ContextType.BINDER) Binder binder, @BindingParam("node") SurveyObject surveyObject) {
		if ( parentSelectorPopUp != null ) {
			CodeAttributeDefinition parentAttrDefn = (CodeAttributeDefinition) surveyObject;
			((CodeAttributeDefinitionFormObject) formObject).setParentCodeAttributeDefinition(parentAttrDefn);
			notifyChange("formObject");
			dispatchApplyChangesCommand(binder);
			closeSchemaNodeSelector();
			notifyChange("dependentCodePaths");
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
