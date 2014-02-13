/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.util.Resources;
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

	private static final String FORM_ID = "fx";
	private static final String LIST_FIELD = "list";
	private Window parentSelectorPopUp;

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") CodeAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}

	@GlobalCommand
	public void codeListsPopUpClosed(@ContextParam(ContextType.BINDER) Binder binder, 
			@BindingParam(CodeListsVM.EDITING_ATTRIBUTE_PARAM) Boolean editingAttribute, 
			@BindingParam(CodeListsVM.SELECTED_CODE_LIST_PARAM) CodeList selectedCodeList) {
		if ( editingAttribute ) {
			if ( selectedCodeList != null ) {
				Form form = getForm(binder);
				form.setField("parentCodeAttribute", null);
				setValueOnFormField(form, LIST_FIELD, selectedCodeList);
				BindUtils.postNotifyChange(null, null, form, "*");
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

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("rootEntity", parentEntity);
		args.put("version", null);
		args.put("title", title);
		CodeAttributeDefinition parentCodeAttributeDefinition = getParentCodeAttributeDefinition();
		args.put("selection", parentCodeAttributeDefinition);
		parentSelectorPopUp = openPopUp(Resources.Component.SCHEMA_TREE_POPUP.getLocation(), true, args);
	}

	private CodeAttributeDefinition getParentCodeAttributeDefinition() {
		try {
			CodeAttributeDefinition parentCodeAttributeDefinition = editedItem.getParentCodeAttributeDefinition();
			return parentCodeAttributeDefinition;
		} catch(Exception e) {
		}
		return null;
	}
	
	@GlobalCommand
	public void closeParentAttributeSelector() {
		if ( parentSelectorPopUp != null ) {
			closePopUp(parentSelectorPopUp);
			parentSelectorPopUp = null;
		}
	}
	
	@GlobalCommand
	public void schemaTreeNodeSelected(@BindingParam("node") SurveyObject surveyObject) {
		CodeAttributeDefinition parentAttrDefn = (CodeAttributeDefinition) surveyObject;
		editedItem.setParentCodeAttributeDefinition(parentAttrDefn);
		//BindUtils.postNotifyChange(null, null, editedItem, "*");
		notifyChange("editedItem");
		closeParentAttributeSelector();
	}

}
