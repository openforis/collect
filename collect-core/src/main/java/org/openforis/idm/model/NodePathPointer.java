package org.openforis.idm.model;

import java.util.HashSet;
import java.util.Set;

import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class NodePathPointer {
	
	private String entityPath;
	private NodeDefinition referencedNodeDefinition;
	
	public NodePathPointer(String entityPath, NodeDefinition childDef) {
		this.entityPath = entityPath;
		this.referencedNodeDefinition = childDef;
	}
	
	public String getEntityPath() {
		return entityPath;
	}
	
	public NodeDefinition getReferencedNodeDefinition() {
		return referencedNodeDefinition;
	}

	public String getChildName() {
		return referencedNodeDefinition.getName();
	}
	
	@Override
	public String toString() {
		return entityPath + "/" + getChildName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityPath == null) ? 0 : entityPath.hashCode());
		result = prime * result + referencedNodeDefinition.getId();
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
		NodePathPointer other = (NodePathPointer) obj;
		if (entityPath == null) {
			if (other.entityPath != null)
				return false;
		} else if (!entityPath.equals(other.entityPath))
			return false;
		if (referencedNodeDefinition.getId() != other.referencedNodeDefinition.getId())
			return false;
		return true;
	}
	
	public static Set<NodePathPointer> filterPointersByVersion(Set<NodePathPointer> pointers, ModelVersion version) {
		if (version == null) {
			return pointers;
		}
		Set<NodePathPointer> result = new HashSet<NodePathPointer>(pointers.size());
		for (NodePathPointer pointer : pointers) {
			if (version.isApplicable(pointer.getReferencedNodeDefinition())) {
				result.add(pointer);
			}
		}
		return result;
	}
}
