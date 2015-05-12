package org.openforis.collect.metamodel.ui;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;


/**
 * 
 * @author S. Ricci
 *
 */
public class UIFormSet extends UIFormContentContainer implements NodeDefinitionUIComponent {

	private static final long serialVersionUID = 1L;
	
	private Integer rootEntityDefinitionId;
	private EntityDefinition rootEntityDefinition;
	private UIConfiguration uiConfiguration;

	UIFormSet(UIConfiguration uiConfiguration, int id) {
		super(null, id);
		this.uiConfiguration = uiConfiguration;
	}

	@Override
	public int getNodeDefinitionId() {
		return getRootEntityDefinitionId();
	}
	
	@Override
	public NodeDefinition getNodeDefinition() {
		return getRootEntityDefinition();
	}
	
	public Integer getRootEntityDefinitionId() {
		return rootEntityDefinitionId;
	}
	
	public void setRootEntityDefinitionId(Integer rootEntityDefinitionId) {
		this.rootEntityDefinitionId = rootEntityDefinitionId;
	}
	
	public EntityDefinition getRootEntityDefinition() {
		if ( rootEntityDefinitionId != null && rootEntityDefinition == null ) {
			this.rootEntityDefinition = (EntityDefinition) getNodeDefinition(this.rootEntityDefinitionId);
		}
		return rootEntityDefinition;
	}

	public void setRootEntityDefinition(EntityDefinition rootEntityDefinition) {
		this.rootEntityDefinition = rootEntityDefinition;
		this.rootEntityDefinitionId = rootEntityDefinition == null ? null: rootEntityDefinition.getId();
	}
	
	@Override
	public UIConfiguration getUIConfiguration() {
		return uiConfiguration;
	}
	
}
