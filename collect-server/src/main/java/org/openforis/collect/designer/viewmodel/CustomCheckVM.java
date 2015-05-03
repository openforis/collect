/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.CustomCheckFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class CustomCheckVM extends CheckVM<CustomCheck> {

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentDefinition") AttributeDefinition parentDefinition,
			@ExecutionArgParam("check") CustomCheck check, @ExecutionArgParam("newItem") Boolean newItem ) {
		super.initInternal(parentDefinition, check, newItem);
	}
	
	@Override
	protected FormObject<CustomCheck> createFormObject() {
		return new CustomCheckFormObject();
	}

}
