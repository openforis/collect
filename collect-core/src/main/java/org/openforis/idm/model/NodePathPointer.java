package org.openforis.idm.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class NodePathPointer {
	
	private String entityPath;
	private String childName;
	
	public NodePathPointer(String entityPath, String childName) {
		this.entityPath = entityPath;
		this.childName = childName;
	}

	public NodeDefinition getReferencedNodeDefinition(NodeDefinition context) {
		String entityDefinitionRelativePath = Path.getAbsolutePath(entityPath);
		EntityDefinition entityDefn = (EntityDefinition) context.getDefinitionByPath(entityDefinitionRelativePath);
		NodeDefinition referencedDefn = entityDefn.getChildDefinition(childName);
		return referencedDefn;
	}
	
	public String getEntityPath() {
		return entityPath;
	}

	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}

	public String getChildName() {
		return childName;
	}

	public void setChildName(String childName) {
		this.childName = childName;
	}

	@Override
	public String toString() {
		return entityPath + "/" + childName;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(entityPath).append(childName).toHashCode();
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
		return new EqualsBuilder().append(childName, other.childName).append(entityPath, other.entityPath).isEquals();
	}

}
