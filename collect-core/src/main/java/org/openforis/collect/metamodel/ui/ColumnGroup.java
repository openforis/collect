/**
 * 
 */
package org.openforis.collect.metamodel.ui;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author S. Ricci
 *
 */
public class ColumnGroup extends TableHeadingComponent implements TableHeadingContainer, NodeDefinitionUIComponent {

	private static final long serialVersionUID = 1L;
	
	private Integer entityDefinitionId;
	private EntityDefinition entityDefinition;
	private List<TableHeadingComponent> headingComponents;

	public ColumnGroup(TableHeadingContainer parent, int id) {
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

	public EntityDefinition getEntityDefinition() {
		if ( entityDefinitionId != null && entityDefinition == null ) {
			this.entityDefinition = (EntityDefinition) getNodeDefinition(entityDefinitionId);
		}
		return entityDefinition;
	}
	
	public void setEntityDefinition(EntityDefinition entityDefinition) {
		this.entityDefinition = entityDefinition;
		this.entityDefinitionId = entityDefinition == null ? null: entityDefinition.getId();
	}
	
	@Override
	public List<TableHeadingComponent> getHeadingComponents() {
		return CollectionUtils.unmodifiableList(headingComponents);
	}
	
	@Override
	public void addHeadingComponent(TableHeadingComponent component) {
		if ( headingComponents == null ) {
			headingComponents = new ArrayList<TableHeadingComponent>();
		}
		headingComponents.add(component);
		getUIConfiguration().attachItem(component);
	}
	
	@Override
	public void removeHeadingComponent(TableHeadingComponent component) {
		headingComponents.remove(component);
		getUIConfiguration().detachItem(component);
	}
	
	@Override
	public Column createColumn() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createColumn(uiOptions.nextId());
	}
	
	@Override
	public Column createColumn(int id) {
		return new Column(this, id);
	}
	
	@Override
	public ColumnGroup createColumnGroup() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createColumnGroup(uiOptions.nextId());
	}
	
	@Override
	public ColumnGroup createColumnGroup(int id) {
		return new ColumnGroup(this, id);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((headingComponents == null) ? 0 : headingComponents.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnGroup other = (ColumnGroup) obj;
		if (headingComponents == null) {
			if (other.headingComponents != null)
				return false;
		} else if (!headingComponents.equals(other.headingComponents))
			return false;
		return true;
	}

}
