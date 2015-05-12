/**
 * 
 */
package org.openforis.collect.metamodel.ui;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author S. Ricci
 *
 */
public class UIFormSection extends UIFormContentContainer implements UIFormComponent, NodeDefinitionUIComponent {

	private static final long serialVersionUID = 1L;

	private Integer entityDefinitionId;
	private EntityDefinition entityDefinition;
	private boolean hidden;
	
	public <P extends UIFormContentContainer> UIFormSection(P parent, int id) {
		super(parent, id);
	}

	@Override
	public int getNodeDefinitionId() {
		return getEntityDefinitionId();
	}
	
	@Override
	public NodeDefinition getNodeDefinition() {
		return getEntityDefinition();
	}
	
	public Integer getEntityDefinitionId() {
		return entityDefinitionId;
	}
	
	public void setEntityDefinitionId(Integer entityDefinitionId) {
		this.entityDefinitionId = entityDefinitionId;
	}
	
	public void setEntityDefinition(EntityDefinition entityDefinition) {
		this.entityDefinition = entityDefinition;
		this.entityDefinitionId = entityDefinition == null ? null: entityDefinition.getId();
	}
	
	public EntityDefinition getEntityDefinition() {
		if ( entityDefinitionId != null && entityDefinition == null ) {
			this.entityDefinition = (EntityDefinition) getNodeDefinition(entityDefinitionId);
		}
		return entityDefinition;
	}

	public void setRootEntityDefinition(EntityDefinition entityDefinition) {
		this.entityDefinition = entityDefinition;
		this.entityDefinitionId = entityDefinition == null ? null: entityDefinition.getId();
	}

	@Override
	public boolean isHidden() {
		return this.hidden;
	}
	
	@Override
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
}
