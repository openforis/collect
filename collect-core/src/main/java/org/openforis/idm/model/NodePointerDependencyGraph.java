package org.openforis.idm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.openforis.commons.collection.Visitor;
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
			List<NodePointer> result = new ArrayList<NodePointer>(defs.size());
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
	protected void visitRelatedItems(NodePointer pointer, NodeDefinition relatedChildDef, String relatedParentEntityPath,
			Visitor<NodePointer> visitor) throws InvalidExpressionException {
		Entity pointerEntity = pointer.getEntity();
		List<Node<?>> relatedParentEntities = Path.parse(relatedParentEntityPath).evaluate(pointerEntity);
		for (Node<?> relatedParentEntity : relatedParentEntities) {
			visitor.visit(new NodePointer((Entity) relatedParentEntity, relatedChildDef));
		}
	}
	
	@Override
	protected void visitRelatedItems(NodePointer pointer, NodeDefinition childDef, Visitor<NodePointer> visitor) {
		visitor.visit(new NodePointer(pointer.getEntity(), childDef));
	}
	
	public List<NodePointer> dependenciesForPointers(Collection<NodePointer> pointers) {
		return super.dependenciesForItems(pointers);
	}
	
	protected Set<NodePathPointer> filterByVersion(Set<NodePathPointer> pointers, ModelVersion version) {
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
	
	static class NodePointerId implements Comparable<NodePointerId> {
		
		private int entityId;
		private int childDefinitionId;

		public NodePointerId(int entityId, int childDefinitionId) {
			this.entityId = entityId;
			this.childDefinitionId = childDefinitionId;
		}

		@Override
		public int compareTo(NodePointerId o) {
			int result = NumberUtils.compare(this.entityId, o.entityId);
			if (result == 0) {
				return NumberUtils.compare(this.childDefinitionId, o.childDefinitionId);
			} else {
				return result;
			}
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

		@Override
		public String toString() {
			return String.format("Entity id: %d - child def id: %d", entityId, childDefinitionId);
		}
	}
	
}