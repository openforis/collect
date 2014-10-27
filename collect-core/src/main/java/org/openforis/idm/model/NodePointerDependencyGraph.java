package org.openforis.idm.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.EntityDefinition;
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
	protected Object getId(NodePointer node) {
		return node.getEntity().getInternalId() + "_" + node.getChildName();
	}

	@Override
	protected String toString(NodePointer node) {
		return getId(node).toString();
	}

	@Override
	protected Collection<NodePointer> toItems(Node<?> node) {
		Set<NodePointer> result = new HashSet<NodePointer>();
		if (node instanceof Entity) {
			EntityDefinition def = (EntityDefinition) node.getDefinition();
			for (NodeDefinition childDef : def.getChildDefinitions()) {
				NodePointer nodePointer = new NodePointer((Entity) node, childDef.getName());
				result.add(nodePointer);
			}
			List<Node<?>> children = ((Entity) node).getChildren();
			for (Node<?> child : children) {
				result.addAll(toItems(child));
			}
		} else if ( node.getParent() != null ) {
			NodePointer nodePointer = new NodePointer(node.getParent(), node.getName());
			result.add(nodePointer);
		}
		return result;
	}

	@Override
	protected Set<NodePointer> determineRelatedItems(NodePointer pointer, String relatedChildName,
			String relatedParentEntityPath) throws InvalidExpressionException {
		Set<NodePointer> result = new HashSet<NodePointer>();
		Entity pointerEntity = pointer.getEntity();
		List<Node<?>> relatedParentEntities = Path.parse(relatedParentEntityPath).evaluate(pointerEntity);
		for (Node<?> relatedParentNode : relatedParentEntities) {
			result.add(new NodePointer((Entity) relatedParentNode, relatedChildName));
		}
		return result;
	}

	@Override
	protected Set<NodePointer> determineRelatedItems(NodePointer pointer, String childName) {
		Entity pointerEntity = pointer.getEntity();
		NodePointer nodePointer = new NodePointer(pointerEntity, childName);
		Set<NodePointer> result = new HashSet<NodePointer>();
		result.add(nodePointer);
		return result;
	}
	
	public List<NodePointer> dependenciesForPointers(Collection<NodePointer> pointers) {
		return super.dependenciesForItems(pointers);
	}

}