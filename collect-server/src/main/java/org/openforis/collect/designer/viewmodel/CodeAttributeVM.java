/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeVM extends AttributeVM<CodeAttributeDefinition> {

	private static final String FORM_ID = "fx";
	private static final String LIST_FIELD = "list";

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
				setValueOnField(form, LIST_FIELD, selectedCodeList);
			}
			validateForm(binder);
		}
	}

	protected Form getForm(Binder binder) {
		Component view = binder.getView();
		return (Form) view.getAttribute(FORM_ID);
	}

	protected void setValueOnField(Form form, String field,
			Object value) {
		form.setField(field, value);
		BindUtils.postNotifyChange(null, null, form, field);
	}

}
