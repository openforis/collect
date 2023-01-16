package org.openforis.idm.model;

import java.util.List;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * @author D. Wiell
 * 
 */
public class NodePointer {
	
	private Entity entity;
	private NodeDefinition childDefinition;
	
	public NodePointer(Node<?> node) {
		this(node.getParent(), node.getDefinition());
	}
	
	public NodePointer(Entity entity, NodeDefinition childDef) {
		if(entity == null) {
			throw new IllegalArgumentException("Entity cannot be null");
		}
		this.entity = entity;
		this.childDefinition = childDef;
	}
	
	public NodePointer(Entity entity, String childName) {
		this(entity, entity.getDefinition().getChildDefinition(childName));
	}

	public Integer getNodesMinCount() {
		return entity.getMinCount(childDefinition);
	}
	
	public Integer getNodesMaxCount() {
		return entity.getMaxCount(childDefinition);
	}
	
	public Entity getEntity() {
		return entity;
	}

	public String getEntityPath() {
		return entity.getPath();
	}
	
	public int getEntityId() {
		return entity.getInternalId();
	}
	
	public NodeDefinition getChildDefinition() {
		return childDefinition;
	}
	
	public int getChildDefinitionId() {
		return childDefinition.getId();
	}
	
	public String getChildName() {
		return childDefinition.getName();
	}

	public List<Node<?>> getNodes() {
		return entity.getChildren(childDefinition);
	}
	
	public boolean areNodesRelevant() {
		return entity.isRelevant(childDefinition);
	}
	
	public Record getRecord() {
		return entity.getRecord();
	}
	
	public ModelVersion getModelVersion() {
		Record record = getRecord();
		return record.getVersion();
	}
	
	public boolean isNodesDeleted() {
		return getRecord() == null;
	}
	
	@Override
	public String toString() {
		return getEntityPath() + "/" + getChildName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + getChildDefinitionId();
		return result;
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
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (getChildDefinitionId() != other.getChildDefinitionId())
			return false;
		return true;
	}

}
