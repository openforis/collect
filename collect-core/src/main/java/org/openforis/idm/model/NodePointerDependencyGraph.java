package org.openforis.idm.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.path.Path;

/**
* 
* @author D. Wiell
* @author S. Ricci
*
*/
public abstract class NodePointerDependencyGraph extends DependencyGraph<NodePointer> {

	public NodePointerDependencyGraph(Survey survey) {
		super(survey);
	}

	protected abstract boolean isDependentItemIncluded(NodePointer node);

	protected abstract Set<NodePathPointer> determineDependents(NodePointer source)
			throws InvalidExpressionException;

	protected abstract Set<NodePathPointer> determineSources(NodePointer dependent)
			throws InvalidExpressionException;

	@Override
	protected List<NodePointer> getChildren(NodePointer node) {
		return Collections.emptyList();
	}

	@Override
	protected NodePointerId getId(NodePointer pointer) {
		return new NodePointerId(pointer.getEntityId(), pointer.getChildDefinitionId());
	}

	@Override
	protected String toString(NodePointer node) {
		return getId(node).toString();
	}

	@Override
	protected Collection<NodePointer> toItems(Node<?> node) {
		if (node instanceof Entity) {
			EntityDefinition def = (EntityDefinition) node.getDefinition();
			List<NodeDefinition> defs = def.getChildDefinitionsInVersion(node.getModelVersion());
			Set<NodePointer> result = new HashSet<NodePointer>(defs.size());
			for (NodeDefinition childDef : defs) {
				result.add(new NodePointer((Entity) node, childDef));
			}
			return result;
		} else if ( node.getParent() != null ) {
			return Collections.singleton(new NodePointer(node));
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	protected Set<NodePointer> determineRelatedItems(NodePointer pointer, NodeDefinition relatedChildDef,
			String relatedParentEntityPath) throws InvalidExpressionException {
		Set<NodePointer> result = new HashSet<NodePointer>();
		Entity pointerEntity = pointer.getEntity();
		List<Node<?>> relatedParentEntities = Path.parse(relatedParentEntityPath).evaluate(pointerEntity);
		for (Node<?> relatedParentNode : relatedParentEntities) {
			result.add(new NodePointer((Entity) relatedParentNode, relatedChildDef));
		}
		return result;
	}

	@Override
	protected Set<NodePointer> determineRelatedItems(NodePointer pointer, NodeDefinition childDef) {
		Entity pointerEntity = pointer.getEntity();
		NodePointer nodePointer = new NodePointer(pointerEntity, childDef);
		return Collections.singleton(nodePointer);
	}
	
	public List<NodePointer> dependenciesForPointers(Collection<NodePointer> pointers) {
		return super.dependenciesForItems(pointers);
	}
	
	protected Set<NodePathPointer> filterByVersion(Set<NodePathPointer> pointers, ModelVersion version) {
		Set<NodePathPointer> result = new HashSet<NodePathPointer>(pointers.size());
		for (NodePathPointer pointer : pointers) {
			if (version == null || version.isApplicable(pointer.getReferencedNodeDefinition())) {
				result.add(pointer);
			}
		}
		return result;
	}

	static class NodePointerId {
		
		private int entityId;
		private int childDefinitionId;

		public NodePointerId(int entityId, int childDefinitionId) {
			this.entityId = entityId;
			this.childDefinitionId = childDefinitionId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + childDefinitionId;
			result = prime * result + entityId;
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
			NodePointerId other = (NodePointerId) obj;
			if (childDefinitionId != other.childDefinitionId)
				return false;
			if (entityId != other.entityId)
				return false;
			return true;
		}

	}
	
}