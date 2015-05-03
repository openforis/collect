/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.PatternCheckFormObject;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.validation.PatternCheck;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class PatternCheckVM extends CheckVM<PatternCheck> {

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentDefinition") AttributeDefinition parentDefinition,
			@ExecutionArgParam("check") PatternCheck check, @ExecutionArgParam("newItem") Boolean newItem ) {
		super.initInternal(parentDefinition, check, newItem);
	}
	
	@Override
	protected FormObject<PatternCheck> createFormObject() {
		return new PatternCheckFormObject();
	}

}
