package org.openforis.idm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	protected Object getId(Node<?> node) {
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
		ArrayList<Node<?>> result = new ArrayList<Node<?>>();
		result.add(node);
		return result;
	}

	@Override
	protected Set<Node<?>> determineRelatedItems(Node<?> node, NodeDefinition relatedChildDef, String relatedParentEntityPath) throws InvalidExpressionException {
		Set<Node<?>> relatedNodes = new HashSet<Node<?>>();
		Entity parent = node.getParent();
		List<Node<?>> relatedParentEntities = new ArrayList<Node<?>>();
		relatedParentEntities = Path.parse(relatedParentEntityPath).evaluate(parent);
		for (Node<?> relatedParentEntity : relatedParentEntities) {
			List<Node<?>> dependentNodes = ((Entity) relatedParentEntity).getAll(relatedChildDef.getName());
			relatedNodes.addAll(dependentNodes);
		}
		return relatedNodes;
	}

	@Override
	protected Set<Node<?>> determineRelatedItems(Node<?> node, NodeDefinition childDef) {
		List<Node<?>> dependentNodes = node.getParent().getAll(childDef.getName());
		Set<Node<?>> relatedNodes = new HashSet<Node<?>>();
		relatedNodes.addAll(dependentNodes);
		return relatedNodes;
	}

	@Override
	protected String toString(Node<?> node) {
		return node.getPath();
	}

}