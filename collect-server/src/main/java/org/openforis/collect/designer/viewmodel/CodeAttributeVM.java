/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeVM extends AttributeVM<CodeAttributeDefinition> {

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") CodeAttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}

	@GlobalCommand
	public void closeCodeListsManagerPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		validateForm(binder);
	}
}
