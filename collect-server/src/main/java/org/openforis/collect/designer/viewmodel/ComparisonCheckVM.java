/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.ComparisonCheckFormObject;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class ComparisonCheckVM extends CheckVM<ComparisonCheck> {

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentDefinition") AttributeDefinition parentDefinition,
			@ExecutionArgParam("check") ComparisonCheck check, @ExecutionArgParam("newItem") Boolean newItem ) {
		super.initInternal(parentDefinition, check, newItem);
	}
	
	@Override
	protected FormObject<ComparisonCheck> createFormObject() {
		return new ComparisonCheckFormObject();
	}

}
