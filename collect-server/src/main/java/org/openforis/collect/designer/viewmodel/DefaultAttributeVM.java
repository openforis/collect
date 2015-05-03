/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class DefaultAttributeVM extends AttributeVM<AttributeDefinition> {

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") AttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.initInternal(parentEntity, attributeDefn, newItem);
	}
	
}
