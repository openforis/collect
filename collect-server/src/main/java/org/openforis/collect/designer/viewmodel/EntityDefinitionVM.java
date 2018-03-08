/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.SurveyObjectFormObject;
import org.openforis.collect.designer.metamodel.NodeType;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

/**
 * @author S. Ricci
 *
 */
public class EntityDefinitionVM extends NodeDefinitionVM<EntityDefinition> {
	
	private static final String ENUMERATE_FIELD_NAME = "enumerate";

	@Init(superclass=false)
	public void init(@ExecutionArgParam("parentEntity") EntityDefinition parentEntity, 
			@ExecutionArgParam("item") EntityDefinition attributeDefn, 
			@ExecutionArgParam("newItem") Boolean newItem,
			@ExecutionArgParam("doNotCommitChangesImmediately") Boolean doNotCommitChangesImmediately) {
		super.initInternal(parentEntity, attributeDefn, newItem);
		boolean doNotCommitChangesImmediatelyBool = doNotCommitChangesImmediately == null ? false: doNotCommitChangesImmediately.booleanValue();
		this.commitChangesOnApply = ! doNotCommitChangesImmediatelyBool;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected SurveyObjectFormObject<EntityDefinition> createFormObject() {
		return (NodeDefinitionFormObject<EntityDefinition>) 
				NodeDefinitionFormObject.newInstance(parentEntity, NodeType.ENTITY, null);
	}
	
	@Command
	public void multipleChanged(@ContextParam(ContextType.BINDER) Binder binder,
			@BindingParam("multiple") Boolean multiple) {
		if (multiple) {
			setTempFormObjectFieldValue(ENUMERATE_FIELD_NAME, true);
		}
		super.multipleChanged(binder, multiple);
	}

}
