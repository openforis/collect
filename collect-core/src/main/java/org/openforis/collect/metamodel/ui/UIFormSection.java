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
	private int column;
	private int columnSpan;
	private int row;
	
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
	public int getColumn() {
		return column;
	}
	
	@Override
	public void setColumn(int column) {
		this.column = column;
	}
	
	@Override
	public int getColumnSpan() {
		return columnSpan;
	}
	
	@Override
	public void setColumnSpan(int columnSpan) {
		this.columnSpan = columnSpan;
	}
	
	@Override
	public int getRow() {
		return row;
	}
	
	@Override
	public void setRow(int row) {
		this.row = row;
	}
}
