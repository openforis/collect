/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.DistanceCheckFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class DistanceCheckVM extends CheckVM<DistanceCheck> {

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentDefinition") AttributeDefinition parentDefinition,
			@ExecutionArgParam("check") DistanceCheck check, @ExecutionArgParam("newItem") Boolean newItem ) {
		super.initInternal(parentDefinition, check, newItem);
	}
	
	@Override
	protected FormObject<DistanceCheck> createFormObject() {
		return new DistanceCheckFormObject();
	}

}
