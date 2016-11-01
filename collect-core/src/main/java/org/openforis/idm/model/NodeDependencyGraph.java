package org.openforis.idm.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openforis.commons.collection.Visitor;
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
public abstract class NodeDependencyGraph extends DependencyGraph<Node<?>> {

	public NodeDependencyGraph(Survey survey) {
		super(survey);
	}

	protected abstract Set<NodePathPointer> determineSources(Node<?> dependent)
			throws InvalidExpressionException;

	protected abstract Set<NodePathPointer> determineDependents(Node<?> source)
			throws InvalidExpressionException;

	protected abstract List<Node<?>> getSortedDependentItems(GraphNode node, Set<GraphNode> unsortedDependents);

	protected abstract boolean isDependentItemIncluded(Node<?> node);

	@Override
	protected Comparable<?> getId(Node<?> node) {
		return node.getInternalId();
	}

	@Override
	protected List<Node<?>> getChildren(Node<?> node) {
		if ( node instanceof Entity ) {
			return ((Entity) node).getChildren();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	protected Collection<Node<?>> toItems(Node<?> node) {
		return Collections.<Node<?>>singleton(node);
	}

	@Override
	protected void visitRelatedItems(Node<?> node, NodeDefinition relatedChildDef, String relatedParentEntityPath,
			Visitor<Node<?>> visitor) throws InvalidExpressionException {
		List<Node<?>> relatedParentEntities = Path.parse(relatedParentEntityPath).evaluate(node.getParent());
		for (Node<?> relatedParentEntity : relatedParentEntities) {
			visitChildren((Entity) relatedParentEntity, relatedChildDef, visitor);
		}
	}
	
	@Override
	protected void visitRelatedItems(Node<?> node, NodeDefinition childDef, final Visitor<Node<?>> visitor) {
		visitChildren(node.getParent(), childDef, visitor);
	}

	private void visitChildren(Entity entity, NodeDefinition childDef, final Visitor<Node<?>> visitor) {
		entity.visitChildren(childDef, new NodeVisitor() {
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				visitor.visit(node);
			}
		});
	}

	@Override
	protected String toString(Node<?> node) {
		return node.getPath();
	}

}