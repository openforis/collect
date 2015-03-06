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
public class UIColumnGroup extends UITableHeadingComponent implements UITableHeadingContainer, NodeDefinitionUIComponent {

	private static final long serialVersionUID = 1L;
	
	private Integer entityDefinitionId;
	private EntityDefinition entityDefinition;
	private List<UITableHeadingComponent> headingComponents;

	public UIColumnGroup(UITableHeadingContainer parent, int id) {
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
	public List<UITableHeadingComponent> getHeadingComponents() {
		return CollectionUtils.unmodifiableList(headingComponents);
	}
	
	@Override
	public void addHeadingComponent(UITableHeadingComponent component) {
		if ( headingComponents == null ) {
			headingComponents = new ArrayList<UITableHeadingComponent>();
		}
		headingComponents.add(component);
		getUIConfiguration().attachItem(component);
	}
	
	@Override
	public void removeHeadingComponent(UITableHeadingComponent component) {
		headingComponents.remove(component);
		getUIConfiguration().detachItem(component);
	}
	
	@Override
	public UIColumn createColumn() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createColumn(uiOptions.nextId());
	}
	
	@Override
	public UIColumn createColumn(int id) {
		return new UIColumn(this, id);
	}
	
	@Override
	public UIColumnGroup createColumnGroup() {
		UIConfiguration uiOptions = getUIConfiguration();
		return createColumnGroup(uiOptions.nextId());
	}
	
	@Override
	public UIColumnGroup createColumnGroup(int id) {
		return new UIColumnGroup(this, id);
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
		UIColumnGroup other = (UIColumnGroup) obj;
		if (headingComponents == null) {
			if (other.headingComponents != null)
				return false;
		} else if (!headingComponents.equals(other.headingComponents))
			return false;
		return true;
	}

}
