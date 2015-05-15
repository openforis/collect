/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.UniquenessCheckFormObject;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class UniquenessCheckVM extends CheckVM<UniquenessCheck> {

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentDefinition") AttributeDefinition parentDefinition,
			@ExecutionArgParam("check") UniquenessCheck check, @ExecutionArgParam("newItem") Boolean newItem ) {
		super.initInternal(parentDefinition, check, newItem);
	}
	
	@Override
	protected FormObject<UniquenessCheck> createFormObject() {
		return new UniquenessCheckFormObject();
	}

}
