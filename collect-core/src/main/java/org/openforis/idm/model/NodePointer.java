package org.openforis.idm.model;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * 
 * @author M. Togna
 * 
 */
public class NodePointer {
	
	private Entity entity;
	private String childName;

	public NodePointer(Node<?> node) {
		this(node.getParent(), node.getName());
	}
	
	public NodePointer(Entity entity, String childName) {
		if(entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}
		this.entity = entity;
		this.childName = childName;
	}

	public Entity getEntity() {
		return entity;
	}

	public String getChildName() {
		return childName;
	}

	public String getEntityPath() {
		return entity.getPath();
	}
	
	public List<Node<?>> getNodes() {
		return entity.getChildren(childName);
	}
	
	public NodeDefinition getChildDefinition() {
		EntityDefinition entityDefn = entity.getDefinition();
		NodeDefinition result = entityDefn.getChildDefinition(childName);
		return result;
	}
	
	public boolean areNodesRelevant() {
		return entity.isRelevant(childName);
	}
	
	public Boolean areNodesRequired() {
		return entity.isRequired(childName);
	}
	
	@Override
	public String toString() {
		return getEntityPath() + "/" + childName;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(entity)
			.append(childName)
			.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodePointer other = (NodePointer) obj;
		return new EqualsBuilder()
				.append(entity, other.entity)
				.append(childName, other.childName)
				.isEquals();
	}

}
