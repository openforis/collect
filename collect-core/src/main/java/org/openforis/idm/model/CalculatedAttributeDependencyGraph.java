package org.openforis.idm.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 * @author D. Wiell
 *
 */
public class CalculatedAttributeDependencyGraph extends NodeDependencyGraph {
	
	public CalculatedAttributeDependencyGraph(Survey survey) {
		super(survey);
	}

	@Override
	protected boolean isDependentItemIncluded(Node<?> node) {
		return (node instanceof Attribute && ((AttributeDefinition) node.getDefinition()).isCalculated());
	}
	
	@Override
	protected List<Node<?>> getSortedDependentItems(Set<GraphNode> toSort) {
		List<Node<?>> result = new GraphSorter(toSort).sort();
		return result;
	}

	@Override
	protected List<Node<?>> getSortedDependentItems(GraphNode node, Set<GraphNode> unsortedDependents) {
		Set<GraphNode> nodes = new HashSet<GraphNode>(unsortedDependents);
		nodes.add(node);
		return getSortedDependentItems(nodes);
	}

	@Override
	protected Set<NodePathPointer> determineDependents(Node<?> source) throws InvalidExpressionException {
		Survey survey = source.getSurvey();
		Set<NodePathPointer> dependentPointers = survey.getDefaultValueDependencies(source.getDefinition());
		return dependentPointers;
	}

	@Override
	protected Set<NodePathPointer> determineSources(Node<?> dependent) throws InvalidExpressionException {
		Survey survey = dependent.getSurvey();
		Set<NodePathPointer> sourcePointers = survey.getDefaultdValueSources(dependent.getDefinition());
		return sourcePointers;
	}
	

}
