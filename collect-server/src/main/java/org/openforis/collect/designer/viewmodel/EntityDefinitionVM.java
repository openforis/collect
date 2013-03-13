/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.model.NodeType;
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
	@SuppressWarnings("unchecked")
	protected SurveyObjectFormObject<EntityDefinition> createFormObject() {
		return (NodeDefinitionFormObject<EntityDefinition>) 
				NodeDefinitionFormObject.newInstance(parentEntity, NodeType.ENTITY, null);
	}

	
	
}
