/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.EntityDefinitionFormObject;
import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class EntityDefinitionVM extends NodeDefinitionVM<EntityDefinition> {

	@Override
	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") EntityDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem) {
		super.init(parentEntity, attributeDefn, newItem);
	}
	
	@Override
	protected SurveyObjectFormObject<EntityDefinition> createFormObject() {
		return new EntityDefinitionFormObject<EntityDefinition>();
	}

	
	
}
