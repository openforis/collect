/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.util.Resources;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeVM extends AttributeVM<CodeAttributeDefinition> {

	private Window codeListsPopUp;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") CodeAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}
	
	@GlobalCommand
	public void openCodeListsManagerPopUp() {
		if ( codeListsPopUp == null ) { 
			dispatchCurrentFormValidatedCommand(true);
			codeListsPopUp = openPopUp(Resources.Component.CODE_LISTS_POPUP.getLocation(), true);
		}
	}

	@GlobalCommand
	public void closeCodeListsManagerPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( codeListsPopUp != null && checkCurrentFormValid() ) {
			closePopUp(codeListsPopUp);
			codeListsPopUp = null;
			validateForm(binder);
		}
	}
	
	
}
