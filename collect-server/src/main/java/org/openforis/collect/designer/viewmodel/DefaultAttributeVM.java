/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;

/**
 * @author S. Ricci
 *
 */
public class DefaultAttributeVM extends AttributeVM<AttributeDefinition> {

	@AfterCompose
	@Override
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		//necessary because of not inheritance of AfterCompose behaviour
		super.afterCompose(view);
	}
	
	@Init(superclass=false)
	@Override
	public void init(@ExecutionArgParam("item") AttributeDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(attributeDefn, newItem);
	}
	
	
}
